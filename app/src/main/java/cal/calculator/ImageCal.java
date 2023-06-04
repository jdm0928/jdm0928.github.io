package cal.calculator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ImageCal extends AppCompatActivity {
    private MyPaintView myView;
    private Paint paint;
    private Path path;
    private Bitmap bitmap;
    private Canvas canvas;
    private Button sendButton;

    File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyDrawings");

    private String serverIp = "192.168.10.114";
    private int serverPort = 7777;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_cal);
        setTitle("간단한 그림판");

        LinearLayout paintLayout = findViewById(R.id.paintLayout);
        myView = new MyPaintView(this);
        paintLayout.addView(myView);

        Button btnCancle = findViewById(R.id.btnCancle);
        btnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myView.saveDrawing();
                String imagePath = dir.getAbsolutePath() + File.separator + "my_drawing1.png";
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                sendImageToServer(bitmap);

            }
        });
    }
    public byte[] convertToBytes(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private void sendImageToServer(Bitmap bitmap){
        byte[] imageBytes = convertToBytes(bitmap);
        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    Socket socket = new Socket(serverIp, serverPort);
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.writeInt(imageBytes.length);
                    dataOutputStream.write(imageBytes);
                    dataOutputStream.close();


                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    String[] array = getImageToServer(inputStream);

                    socket.close();

                    Intent intent = new Intent();
                    intent.putExtra("textarray",array);
                    setResult(RESULT_OK, intent);
                    finish();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public static String[] getImageToServer(DataInputStream inputStream) throws IOException {
        int len = inputStream.readInt();

        if(len>0){
            String[] textArray = new String[len];

            for(int i=0; i<len; i++){
                int textlen = inputStream.readInt();
                byte[] textBytes = new byte[textlen];
                inputStream.readFully(textBytes);
                String text = new String(textBytes);
                textArray[i] = text;
            }
            return textArray;
        }
        return null;
    }
    private class MyPaintView extends View {
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mPaint;

        public MyPaintView(Context context) {
            super(context);
            mPath = new Path();
            mPaint = new Paint();
            mPaint.setColor(Color.BLACK);
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(25);
            mPaint.setStyle(Paint.Style.STROKE);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
            canvas.drawPath(mPath, mPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPath.reset();
                    mPath.moveTo(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    mPath.lineTo(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    mPath.lineTo(x, y);
                    mCanvas.drawPath(mPath, mPaint);
                    mPath.reset();
                    break;
            }
            invalidate();
            return true;
        }

        public void saveDrawing() {
            String fileName = "my_drawing1.png";

            // SDCard(ExternalStorage) : 외부저장공간
            // 접근하려면 반드시 AndroidManifest.xml에 권한 설정을 한다.
            if (!dir.exists())
                dir.mkdirs();

            File file = new File(dir, fileName);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Toast.makeText(getApplicationContext(), "그림이 저장되었습니다.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "그림 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}