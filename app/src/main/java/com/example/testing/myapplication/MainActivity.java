package com.example.testing.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.ByteArrayOutputStream;


public class MainActivity extends ActionBarActivity {

    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private EditText name;
    private EditText phone;
    private EditText email;
    private EditText address;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "YDctI4mAu2YPFbxYGxjz2kiJCddmH0EwGtPdYp0C", "6oDl5ky0fQRhP7puz4gBNrBHy7LSGmoXHFbTz7Rz");

        this.imageView = (ImageView)this.findViewById(R.id.photo);
        Button photoButton = (Button) this.findViewById(R.id.submit);
        name = (EditText) this.findViewById(R.id.name);
        phone = (EditText) this.findViewById(R.id.phone);
        email = (EditText) this.findViewById(R.id.email);
        address = (EditText) this.findViewById(R.id.address);

        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        final Button submitButton = (Button) this.findViewById(R.id.submit);
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isSuccess = submitDetailsToCloud();
                showMessageDialog(isSuccess);
            }
        });
    }

    private void showMessageDialog(final boolean isSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(isSuccess)
            builder = builder.setMessage(R.string.push_success);
        else
            builder = builder.setMessage(R.string.push_failure);

        builder.setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (isSuccess) {
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.icon));
                    name.setText("");
                    phone.setText("");
                    email.setText("");
                    address.setText("");
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean submitDetailsToCloud() {
        try {
            ParseObject parseObject = new ParseObject("Contractor");
            parseObject.put("name", name.getText().toString());
            parseObject.put("phone", phone.getText().toString());
            parseObject.put("email", email.getText().toString());
            parseObject.put("address", address.getText().toString());
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            ParseFile file = new ParseFile(phone.getText().toString()+".jpg",byteArray);
            parseObject.put("myImage",file);
            parseObject.saveInBackground();
        }catch (Exception e) {
            return false;
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
