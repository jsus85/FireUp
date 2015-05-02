package com.bambazu.fireup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.bambazu.fireup.Adapter.ServiceAdapter;
import com.bambazu.fireup.Adapter.ViewPagerAdapter;
import com.bambazu.fireup.Config.Config;
import com.bambazu.fireup.Helper.DistanceManager;
import com.bambazu.fireup.Interfaz.CalculateDistanceListener;
import com.bambazu.fireup.Model.Place;
import com.bambazu.fireup.Model.Service;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.viewpagerindicator.UnderlinePageIndicator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;

public class Detail extends ActionBarActivity implements View.OnClickListener, CalculateDistanceListener {
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private ArrayList<String> place_image;
    private int totalImages = 1;
    private UnderlinePageIndicator indicator;
    private AQuery imgLoader;
    private TextView placeName;
    private RatingBar placeRating;
    private TextView placeCategory;
    private TextView placeLowPrice;
    private TextView placeHighPrice;
    private TextView placeDescription;
    private ImageView placeLocation;
    private TextView placeAddress;
    private TextView placePhone;
    private TextView placeCity;
    private TextView placeDistance;

    private Button gridServices;

    private Button btnShowDescription;
    private static String desc;
    private  static String latitude;
    private static String longitude;
    private LinearLayout linearLayoutForMap;
    private String objectId;
    private ListView listServices;
    private View serviceWrapper;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imgLoader = new AQuery(this);

        viewPager = (ViewPager) findViewById(R.id.place_pager);

        placeName = (TextView) findViewById(R.id.detail_place_name);
        placeRating = (RatingBar) findViewById(R.id.detail_place_rating);
        placeCategory = (TextView) findViewById(R.id.detail_place_category);
        placeLowPrice = (TextView) findViewById(R.id.detail_low_price);
        placeHighPrice = (TextView) findViewById(R.id.detail_high_price);

        placeLocation = (ImageView) findViewById(R.id.detail_place_location);
        placeLocation.setOnClickListener(this);

        linearLayoutForMap = (LinearLayout) findViewById(R.id.map_info_wrapper);
        linearLayoutForMap.setOnClickListener(this);

        placeAddress = (TextView) findViewById(R.id.detail_place_address);
        placePhone = (TextView) findViewById(R.id.detail_place_phone);
        placeCity = (TextView) findViewById(R.id.detail_place_city);
        placeDistance = (TextView) findViewById(R.id.detail_place_distance);

        gridServices = (Button) findViewById(R.id.gridServices);
        gridServices.setOnClickListener(this);

        placeDescription = (TextView) findViewById(R.id.detail_description);

        btnShowDescription = (Button) findViewById(R.id.btn_show_more_desc);
        btnShowDescription.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            Place placeData = Config.currentPlaces.get(extras.getInt("placePosition"));
            setTitle(placeData.getPlaceName());
            objectId = placeData.getPlaceCode();
            showPlaceData(placeData);

            //Calculate distance
            final HashMap<String, Object> queryData = new HashMap<String, Object>();
            queryData.put("destinationLatitude", placeData.getLatitude());
            queryData.put("destinationLongitude", placeData.getLongitude());

            final DistanceManager distance = new DistanceManager();
            distance.setCalculateDistanceListener(this);
            distance.execute(queryData);
        }
        else{
            Toast.makeText(this, getResources().getString(R.string.error_place_detail_data), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_call) {
            callPlace();
            return true;
        }
        else if (id == R.id.action_comments) {
            openComments();
            return true;
        }
        else if(id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_show_more_desc:
                showModalDescription();
                break;

            case R.id.gridServices:
                showServices();
                break;

            case R.id.detail_place_location:
            case R.id.map_info_wrapper:
                //Intent i = new Intent(Detail.this, Map.class);
                //i.putExtra("latitude", latitude);
                //i.putExtra("longitude", longitude);
                //startActivity(i);
                break;
        }
    }

    @Override
    public void calculateDistance(String distance) {
        placeDistance.setText(distance);
    }

    private void showPlaceData(Place placeData){
        place_image = new ArrayList<String>();

        if(placeData.getPlaceIcon() != null){
            place_image.add(placeData.getPlaceIcon());
        }

        if(placeData.getPlaceIcon2() != null){
            place_image.add(placeData.getPlaceIcon2());
        }

        if(placeData.getPlaceIcon3() != null){
            place_image.add(placeData.getPlaceIcon3());
        }

        if(placeData.getPlaceIcon4() != null){
            place_image.add(placeData.getPlaceIcon4());
        }

        if(placeData.getPlaceIcon5() != null){
            place_image.add(placeData.getPlaceIcon5());
        }

        totalImages = place_image.size();
        pagerAdapter = new ViewPagerAdapter(Detail.this, place_image, totalImages);
        viewPager.setAdapter(pagerAdapter);

        indicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
        indicator.setFades(false);
        indicator.setViewPager(viewPager);

        placeName.setText(placeData.getPlaceName());
        placeCategory.setText(getApplicationContext().getResources().getIdentifier(placeData.getPlaceCategory().toLowerCase(), "string", getApplicationContext().getPackageName()));
        placeRating.setRating(Float.valueOf(placeData.getPlaceRanking()));

        placeLowPrice.setText(formatCurrency(placeData.getLowprice()));
        placeHighPrice.setText(formatCurrency(placeData.getHighprice()));

        latitude = String.valueOf(placeData.getLatitude());
        longitude = String.valueOf(placeData.getLongitude());
        String urlMapImage = "http://maps.google.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=17&size=450x350&scale=4&sensor=false";

        AQuery asyncLoader = imgLoader.recycle(placeLocation);
        asyncLoader.id(placeLocation).progress(R.drawable.no_image_available).image(urlMapImage, true, true, 0, R.drawable.no_image_available, null, AQuery.FADE_IN);

        placeAddress.setText(placeData.getAddress());
        placePhone.setText(placeData.getPhone().toString().substring(3));
        placeCity.setText(placeData.getCity().toString() + " - " + placeData.getDepto().toString());

        placeDescription.setText(showPlaceDescription(placeData.getDescription()));
        phone = placeData.getPhone();
    }

    private String showPlaceDescription(String placeDescription){
        desc = placeDescription;
        String placeDesc = null;

        if(placeDescription.length() > 150){
            placeDesc = placeDescription.substring(0, 150) + "...";
            btnShowDescription.setVisibility(View.VISIBLE);
        }
        else{
            placeDesc = placeDescription;
            btnShowDescription.setVisibility(View.INVISIBLE);
        }

        return placeDesc;
    }

    private void showModalDescription(){
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View comment_wrapper = inflater.inflate(R.layout.description_dialog, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(Detail.this);
        dialog.setView(comment_wrapper);

        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        final TextView comment_value = (TextView) comment_wrapper.findViewById(R.id.descrption_text);
        comment_value.setText(desc);

        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    private String formatCurrency(Number price){
        String sign = "$ ";
        NumberFormat defaultFormat = NumberFormat.getInstance();
        Currency currency = Currency.getInstance("COP");
        defaultFormat.setCurrency(currency);

        return sign + defaultFormat.format(price);
    }

    private void showServices(){
        final ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery("Places");
        innerQuery.whereEqualTo("objectId", objectId);

        final ParseQuery<ParseObject> query = ParseQuery.getQuery("ServicesByPlaces");
        query.whereMatchesQuery("idPlace", innerQuery);
        query.include("idService");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        ArrayList<Service> arrayServices = null;
                        arrayServices = new ArrayList<Service>();

                        for (int i = 0; i < list.size(); i++) {
                            ParseObject services = list.get(i);
                            arrayServices.add(
                                    new Service(
                                            services.getParseObject("idService").getParseFile("icon").getUrl(),
                                            services.getParseObject("idService").getString("name")
                                    )
                            );
                        }

                        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                        serviceWrapper = inflater.inflate(R.layout.place_services, null);

                        listServices = (ListView) serviceWrapper.findViewById(R.id.listServices);
                        listServices.setAdapter(new ServiceAdapter(getApplicationContext(), arrayServices));

                        AlertDialog.Builder dialog = new AlertDialog.Builder(Detail.this);
                        dialog.setView(serviceWrapper);

                        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });

                        AlertDialog alertDialog = dialog.create();
                        alertDialog.show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_services), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void callPlace(){
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    private void openComments(){
        Intent intent = new Intent(getApplicationContext(), Comment.class);
        intent.putExtra("objectId", objectId);
        startActivity(intent);
    }
}
