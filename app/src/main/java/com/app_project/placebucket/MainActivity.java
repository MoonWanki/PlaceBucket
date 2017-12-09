package com.app_project.placebucket;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity{


    private final int REQUEST_CODE_BUCKET = 201;
    private final int REQUEST_CODE_ADD_BUCKET = 202;

    private final long FINISH_INTERVAL_TIME = 1500;
    private long backPressedTime = 0;
    private DrawerLayout mDrawerLayout;
    ImageView userImage;
    Toolbar toolbar;
    TextView plzAddBucket;
    Button logoutButton;
    FloatingActionButton floatingActionButton;
    ListView listView;
    BucketListAdapter adapter;
   // CollapsingToolbarLayout toolbar;
    SwipeRefreshLayout swipeBucket;

    // Progress Dialog
    private ProgressDialog pDialog;

    private ArrayList<SingleBucket> bucketArray;

    private static final String url_get_bucket = "http://18.216.36.241/pb/get_all_buckets.php";

    // JSON Node names
    protected static final String TAG_SUCCESS = "success";
    protected static final String TAG_BUCKETS = "buckets";
    protected static final String TAG_BNO = "Bno";
    protected static final String TAG_BNAME = "Bname";
    protected static final String TAG_IMG = "img";


    private static final String TAG = "MyTag";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


           toolbar=findViewById(R.id.toolbar);
        {
            setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
            final ActionBar ab = getSupportActionBar();
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);


        }

        {
            mDrawerLayout = findViewById(R.id.drawer_layout);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this,mDrawerLayout,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            mDrawerLayout.setDrawerListener(toggle);
            toggle.syncState();
            NavigationView navigationView =  findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(

                    new NavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(MenuItem menuItem) {

                            int id = menuItem.getItemId();

                            if (id == R.id.nav_share) {

                                Intent msg = new Intent(Intent.ACTION_SEND);

                                msg.addCategory(Intent.CATEGORY_DEFAULT);

                                msg.putExtra(Intent.EXTRA_SUBJECT, "PlaceBucket!");

                                msg.putExtra(Intent.EXTRA_TEXT, "같이쓰자 ㅎ");

                                msg.putExtra(Intent.EXTRA_TITLE, "제목");

                                msg.setType("text/plain");

                                startActivity(Intent.createChooser(msg, "공유"));


                            } else if (id == R.id.nav_send) {

                            }
                            menuItem.setChecked(true);
                            mDrawerLayout.closeDrawers();
                            return true;
                        }
                    });
        }



        plzAddBucket = findViewById(R.id.plzAddBucket);
       // logoutButton = findViewById(R.id.logoutButton);
        floatingActionButton = findViewById(R.id.fab_main);
        listView = findViewById(R.id.list_bucket);

        String name = Profile.getCurrentProfile().getName();
        String id = Profile.getCurrentProfile().getId();

       /* logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewLogoutDialog();
            }
        });*/

        // Floating Button - go to AddBucketActivity
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddBucketActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_BUCKET);
            }
        });

        // new GetUser().execute(url_get_user);
        swipeBucket = findViewById(R.id.swipeBucket);



        swipeBucket.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new LoadAllBuckets().execute(url_get_bucket + "?uid=" + Profile.getCurrentProfile().getId());
            }
        });

        new LoadAllBuckets().execute(url_get_bucket + "?uid=" + Profile.getCurrentProfile().getId());

        try { setToken(Profile.getCurrentProfile().getId(), FirebaseInstanceId.getInstance().getToken()); } catch (Exception e) { e.printStackTrace(); }

    }

    public void setToken(String id, String token) throws Exception {
        Request request = new Request.Builder()
                .url("http://18.216.36.241/pb/register_fcm_token.php?id=" + id + "&token=" + token).build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {

            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {

                ResponseBody responseBody = response.body();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }

                Log.d(TAG, responseBody.string());
            }
        });
    }


    class BucketListAdapter extends BaseAdapter {

        ArrayList<SingleBucket> items;

        public void setList(ArrayList<SingleBucket> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View v, ViewGroup viewGroup) {
            SingleBucketView view = new SingleBucketView(getApplicationContext());
            SingleBucket item = items.get(i);
            view.setBnoView(item.getNo());
            view.setBnameView(item.getName());
            view.setBgImgView(item.getImage());

            return view;
        }

    }

    class LoadAllBuckets extends AsyncTask<String, Void, String> {

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(!swipeBucket.isRefreshing()) {
                pDialog = new ProgressDialog(MainActivity.this);
                pDialog.setMessage("버킷 목록을 불러오는 중입니다...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
            }

        }

        // getting All products from url
        protected String doInBackground(String... params) {

            String uri = params[0];
            BufferedReader bufferedReader = null;

            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                StringBuilder sb = new StringBuilder();

                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String json;
                while((json=bufferedReader.readLine())!=null) {
                    sb.append(json + "\n");
                }

                return sb.toString().trim();

            } catch(Exception e) {
                return null;
            }

        }

        protected void onPostExecute(String result) {

            pDialog.dismiss();

            if(result!=null) {

                try {

                    bucketArray = new ArrayList<>();

                    JSONObject jsonObject = new JSONObject(result);

                    int success = jsonObject.getInt(TAG_SUCCESS);

                    if (success == 0) {
                        plzAddBucket.setText("버킷을 추가하세요.");

                    } else if(success == 1) {

                        plzAddBucket.setText("");

                        JSONArray jsonArray = jsonObject.getJSONArray(TAG_BUCKETS);
                        // updating UI from Background Thread

                        ArrayList<String> nos = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject j = jsonArray.getJSONObject(i);
                            String bno = j.getString(TAG_BNO);
                            String bname = j.getString(TAG_BNAME);

                            nos.add(bno); // for putting into GetImage thread

                            SingleBucket b = new SingleBucket(bno, bname);
                            bucketArray.add(b);
                        }

                        GetImages gi = new GetImages();
                        gi.setNos(nos);
                        Log.d("mytag:nos", nos.get(0));
                        Thread thread = new Thread(gi);
                        thread.start();
                        thread.join();
                        ArrayList<Bitmap> images = gi.getImages();
                        Log.d("mytag:images num", String.valueOf(images.size()));

                        for(int i=0 ; i<images.size() ; i++) {
                            Bitmap b = gi.getBitmap(i);
                            if(b==null) {
                                Log.d("mytag", "null");

                            } else {
                                Log.d("mytag:images num", b.toString());
                                bucketArray.get(i).setImage(b);
                            }
                        }

                        adapter = new BucketListAdapter();
                        adapter.setList(bucketArray);

                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                SingleBucket b = (SingleBucket) adapter.getItem(i);
                                Intent intent = new Intent(getApplicationContext(), BucketActivity.class);
                                intent.putExtra(TAG_BNO, b.getNo());
                                intent.putExtra(TAG_BNAME, b.getName());
                                startActivityForResult(intent, REQUEST_CODE_BUCKET);
                            }


                        });


                        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, int i, long l) {
                                viewBucketMenuDialog(i);
                                return true;
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "JSON response is null.", Toast.LENGTH_SHORT).show();
            }

            if(swipeBucket.isRefreshing()) {
                swipeBucket.setRefreshing(false);
            }

        }

    }

    protected  void viewBucketMenuDialog(final int position) {
        CharSequence menu[] = new CharSequence[] {"버킷 상단 고정", "나가기"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(bucketArray.get(position).getName());
        builder.setItems(menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch(which) {
                    case 0:
                        Toast.makeText(getApplicationContext(), "지원하지 않는 기능입니다.", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        try {
                            delBucket(bucketArray.get(position).getNo());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

    protected void viewLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("정말 로그아웃 하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                LoginManager.getInstance().logOut();
                finish();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_CODE_ADD_BUCKET) {
            if (resultCode == Activity.RESULT_OK) {
                new LoadAllBuckets().execute(url_get_bucket + "?uid=" + Profile.getCurrentProfile().getId());
            }
        }
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            Intent intent = new Intent();
            String strFlag = "exit";
            intent.putExtra("value", strFlag);
            setResult(RESULT_OK, intent);
            super.onBackPressed();
        }
        else
        {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "뒤로 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case R.id.logout:
                viewLogoutDialog();
                return true;
            case R.id.settings:
                Toast.makeText(getApplicationContext(), "힝 속았징", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void delBucket(String bno) throws Exception {
        Request request = new Request.Builder()
                .url("http://18.216.36.241/pb/set_member.php?mode=del&id="+Profile.getCurrentProfile().getId()+"&bno="+bno).build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {

            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {

                ResponseBody responseBody = response.body();
                try {
                    JSONObject result = new JSONObject(responseBody.string());

                    int success = result.getInt("success");
                    final String msg = result.getString("message");

                    if(success==1)
                    {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                new LoadAllBuckets().execute(url_get_bucket + "?uid=" + Profile.getCurrentProfile().getId());
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

}
