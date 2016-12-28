package com.example.laptop.pictureprob;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import java.util.ArrayList;
import java.util.List;


import Model.Measures;
import Model.Nutrients;
import Model.Utilities;
import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    ImageView imageToUpl;
    Button uppImage;
    ListView lv;
    TableLayout gridView;

    List<String> listItem = new ArrayList<String>() ;
    private static final int RESULT_LOAD_IMAGE =1;
    private static String PATH ="";
    Utilities utilities = new Utilities();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageToUpl = (ImageView)findViewById(R.id.imageToUpload);
        imageToUpl.setOnClickListener(this);
        uppImage = (Button)findViewById(R.id.brnUploadImage);
        uppImage.setOnClickListener(this);
        uppImage.setVisibility(View.INVISIBLE);
        lv = (ListView) findViewById(R.id.listitem);
        gridView = (TableLayout)findViewById(R.id.tableNutri);
        gridView.setVisibility(View.INVISIBLE);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String item = listItem.get(position);
                PullFromUSDA fromUSDA = new PullFromUSDA();
                fromUSDA.name = item;
                fromUSDA.execute();

            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.imageToUpload:
                if(imageToUpl!=null) {

                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                    uppImage.setVisibility(View.VISIBLE);
                }
                else{Toast.makeText(this,"Pick a picture from a gallery first",Toast.LENGTH_LONG).show();}
                    break;
            case R.id.brnUploadImage:
                // Bitmap image = ((BitmapDrawable)imageToUpl.getDrawable()).getBitmap();
                PullFromClarifai fromClarifai = new PullFromClarifai(PATH);
                fromClarifai.execute();
                listItem = fromClarifai.resList;
                break;



        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RESULT_LOAD_IMAGE && resultCode ==RESULT_OK && data!=null)
        {
            Uri selectImage = data.getData();
            PATH = utilities.getRealPathFromURI(MainActivity.this,selectImage);

            imageToUpl.setImageURI(selectImage);


        }
    }
private class PullFromUSDA extends AsyncTask<Void,Void,Void>
{
    String  name ="";
    String id="";
    private List<Nutrients> parent;
    private List<Measures> child;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected Void doInBackground(Void... params) {
        String urlsearch ="http://api.nal.usda.gov/ndb/search/?format=json&q="+name+"&fg=Fruits%20and%20Fruit%20Juices&sort=r&max=25&ds=Standard%20Reference&offset=1&api_key=PtZV0AYQmhFCe2CqX454ns6q6RGOmEO0fJRefSMN";

        HttpHandler handler = new HttpHandler();
        String jsonStr = handler.makeServiceCall(urlsearch);

        if(jsonStr!=null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONObject json2 = new JSONObject(jsonObj.getString("list"));
                JSONArray items = new JSONArray(json2.getString("item"));

                String nb = items.getJSONObject(0).getString("ndbno");

                id=nb;
                String urlreport = "http://api.nal.usda.gov/ndb/reports/?ndbno="+id+"&type=b&format=json&api_key=PtZV0AYQmhFCe2CqX454ns6q6RGOmEO0fJRefSMN";


                String result =  handler.makeServiceCall(urlreport);

                if(result!=null)
                {
                    List<Nutrients> nutrientsList = new ArrayList<Nutrients>();


                    JSONObject jo = new JSONObject(result);
                    JSONObject jsonjo = new JSONObject(jo.getString("report"));
                    JSONObject jsonjo2 = new JSONObject(jsonjo.getString("food"));
                    JSONArray jsonjo3 = new JSONArray(jsonjo2.getString("nutrients"));

                    for(int i=0;i<jsonjo3.length();i++)
                    {
                        Nutrients nutie = new Nutrients();
                        JSONObject n = jsonjo3.getJSONObject(i);

                        nutie.setNutr_id(Integer.parseInt(n.getString("nutrient_id")));
                        nutie.setName(n.getString("name"));
                        nutie.setGroup(n.getString("group"));
                        nutie.setUnit(n.getString("unit"));
                        nutie.setValue(n.getString("value"));
                        nutrientsList.add(nutie);


                        JSONArray jsonmeasures = new JSONArray(jsonjo3.getJSONObject(i).getString("measures"));
                        List<Measures> measuresList = new ArrayList<Measures>();
                        for (int j = 0; j <jsonmeasures.length() ; j++)
                        {

                            Measures mes = new Measures();
                           JSONObject mesOb = jsonmeasures.getJSONObject(j);

                           /* mes.setLabel(mesOb.getString("label"));
                            mes.setEqv(mesOb.getString("eqv"));
                            mes.setQty(mesOb.getString("qty"));*/
                            mes.setValue(mesOb.getString("value"));

                            measuresList.add(mes);
                            child=measuresList;

                        }
                        nutie.setMeasuresDetails((ArrayList<Measures>) child);
                        parent=nutrientsList;



                    }
                }


            }
            catch (Exception e)
            {

                e.printStackTrace();
            }
        }

                return null;
    }

    @Override
    protected void onPostExecute(Void result)
    {
     super.onPostExecute(result);
        gridView.setVisibility(View.VISIBLE);

        try
        {

            if (parent != null && parent.size()>0)
            {
                for(Nutrients temp: parent)
                {

                    TableRow tableRow = new TableRow(MainActivity.this);
                    TextView tx = new TextView(MainActivity.this);
                    tx.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.table_border));

                    tx.setText(temp.getName());
                    tx.setPadding(4, 4, 4, 4);
                    tx.setGravity( Gravity.CENTER);
                    tableRow.addView(tx);

                    tx = new TextView(MainActivity.this);
                    tx.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.table_border));

                    tx.setText(temp.getUnit());
                    tx.setPadding(4, 4, 4, 4);
                    tx.setGravity(Gravity.CENTER);
                    tableRow.addView(tx);

                    tx = new TextView(MainActivity.this);
                    tx.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.table_border));

                    tx.setText( temp.getValue());
                    tx.setPadding(4, 4, 4, 4);
                    tx.setGravity(Gravity.CENTER);
                    tableRow.addView(tx);


                   List<Measures> measuresArrayList= new ArrayList<Measures>();
                    measuresArrayList=temp.getMeasuresDetails();

                    for(Measures m:measuresArrayList)
                    {
                        tx = new TextView(MainActivity.this);
                        tx.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.table_border));

                         tx.setText(m.getValue());
                        tx.setPadding(4, 4, 4, 4);
                        tx.setGravity(Gravity.CENTER);
                        tableRow.addView(tx);
                    }

                gridView.addView(tableRow);


                }

            }
        }

        catch(Exception e){e.printStackTrace();}

    }

}
    private  class PullFromClarifai extends AsyncTask<Object, Object, List<String>>
    {

        Bitmap image;
        String path;
        Context context;
        public List<String> resList = new ArrayList<String>();
        Dialog progress = new Dialog(MainActivity.this);
        public PullFromClarifai(String path) {
          //  this.image = image;
            this.path = path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           progress.setContentView(R.layout.cirkural_progress_bar);
            progress.setTitle("Processing picture,please wait...");
            progress.show();
        }

        @Override
        protected List<String> doInBackground(Object... params) {


           /* ByteArrayOutputStream byteArrayOutputStream  = new ByteArrayOutputStream(); //one of the ways for sending image
            image.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            String encoded = Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);*/
           // System.gc();

                        final ClarifaiClient client = new ClarifaiBuilder("_6k_RoStJW5kX6SNIEpdaC9rynEXdq8gsiQveOgU","FXI-qfLNDXku6oIRWX56TV_WeG-CA7lgUjH7AKDN")
                                .client(new OkHttpClient()).buildSync();

                   final List<ClarifaiOutput<Concept>>  results =    client.getDefaultModels().foodModel().predict()
                                .withInputs(ClarifaiInput.forImage(ClarifaiImage.of(new File(path)))).executeSync().get();

                if(results!=null && results.size()>0)
                {
                    for (int i=0;i<results.size();i++)
                    {
                    ClarifaiOutput<Concept> clarifaiOutput =results.get(i);
                        List<Concept> concepts = clarifaiOutput.data();
                        if (concepts!=null && concepts.size()>0)
                        {
                            for (int j=0;j<5;j++)
                            {
                                resList.add(concepts.get(j).name());
                            }
                        }
                    }
                }

            return resList;
        }

        @Override
        protected void onPostExecute(final List<String> str) {
            super.onPostExecute(str);


            progress.dismiss();
            progress.cancel();
             for (String result : str)
            {
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,str);
                lv.setAdapter(arrayAdapter);

            }


        }


    }


}
