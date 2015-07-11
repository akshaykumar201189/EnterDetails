package com.example.testing.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private ImageView identityImage;
    private EditText name;
    private EditText phone;
    private EditText depot;
    private EditText address;
    private Spinner identityType;
    private EditText identityNumber;


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
        depot = (EditText) this.findViewById(R.id.depot);
        address = (EditText) this.findViewById(R.id.address);
        identityType = (Spinner) this.findViewById(R.id.identityType);
        identityNumber = (EditText) this.findViewById(R.id.identityText);
        identityImage = (ImageView) this.findViewById(R.id.identityImage);

        addItemsOnSpinner(identityType);

        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage(1,2);
            }
        });

        final Button identityButton = (Button) this.findViewById(R.id.identityButton);
        identityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(3,4);
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

    private void addItemsOnSpinner(Spinner spinner) {
        List<String> list = new ArrayList<String>();
        list.add("PAN");
        list.add("DL");
        list.add("PASSPORT");
        list.add("VOTERID");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    private void selectImage(final int a, final int b) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, a);
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, b);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
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
                    identityImage.setImageDrawable(getResources().getDrawable(R.mipmap.icon1));
                    name.setText("");
                    phone.setText("");
                    depot.setText("");
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
            parseObject.put("depot", depot.getText().toString());
            parseObject.put("address", address.getText().toString());
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            ParseFile file = new ParseFile(phone.getText().toString()+".jpg",byteArray);
            parseObject.put("myImage",file);
            parseObject.put("identityType",identityType.getSelectedItem().toString());
            parseObject.put("identityNumber", identityNumber.getText().toString());
            Bitmap bitmap2 = ((BitmapDrawable) identityImage.getDrawable()).getBitmap();
            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
            bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
            byte[] byteArray2 = stream2.toByteArray();
            ParseFile file2 = new ParseFile(identityNumber.getText().toString()+".jpg",byteArray2);
            parseObject.put("identityImage",file2);
            parseObject.saveInBackground();
        }catch (Exception e) {
            return false;
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            imageView.setImageBitmap(getPhotoFromCameraIntent(data));
        }
        else if (requestCode == 2 && resultCode == RESULT_OK) {
            imageView.setImageBitmap(getPhotoFromGalleryImageIntent(data));
        }
        else if (requestCode == 3 && resultCode == RESULT_OK) {
            identityImage.setImageBitmap(getPhotoFromCameraIntent(data));
        }
        else if (requestCode == 4 && resultCode == RESULT_OK) {
            identityImage.setImageBitmap(getPhotoFromGalleryImageIntent(data));
        }
    }

    private Bitmap getPhotoFromCameraIntent(Intent data) {
        Bitmap photo = (Bitmap) data.getExtras().get("data");
        return photo;
    }

    private Bitmap getPhotoFromGalleryImageIntent(Intent data) {
        Uri selectedImage = data.getData();
        String[] filePath = { MediaStore.Images.Media.DATA };
        Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
        c.moveToFirst();
        int columnIndex = c.getColumnIndex(filePath[0]);
        String picturePath = c.getString(columnIndex);
        c.close();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap photo = (BitmapFactory.decodeFile(picturePath,options));
        return photo;
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
