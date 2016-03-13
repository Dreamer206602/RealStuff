package com.example.ivor_hu.meizhi.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.toolbox.RequestFuture;
import com.example.ivor_hu.meizhi.APP;
import com.example.ivor_hu.meizhi.StuffFragment;
import com.example.ivor_hu.meizhi.db.DBManager;
import com.example.ivor_hu.meizhi.db.Stuff;
import com.example.ivor_hu.meizhi.utils.Constants;
import com.example.ivor_hu.meizhi.utils.DateUtil;
import com.example.ivor_hu.meizhi.utils.VolleyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Ivor on 2016/3/3.
 */
public class StuffFetchService extends IntentService {
    private static final String TAG = "StuffFetchService";

    public static final String ACTION_UPDATE_RESULT = "com.ivor.meizhi.update_result";
    public static final String EXTRA_FETCHED = "fetched";
    public static final String EXTRA_TRIGGER = "trigger";
    public static final String EXTRA_TYPE = "type";
    public static final String ACTION_FETCH_REFRESH = "com.ivor.meizhi.fetch_refresh";
    public static final String ACTION_FETCH_MORE = "com.ivor.meizhi.fetch_more";

    private String type, latestUrl, typeName;
    private LocalBroadcastManager localBroadcastManager;

    public StuffFetchService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        type = intent.getStringExtra(StuffFragment.SERVICE_TYPE);
        Log.d(TAG, "onHandleIntent: " + type);
        latestUrl = Constants.getLatestUrlFromType(type);
        typeName = Constants.getTypeNameFromType(type);

        List<Stuff> latest = DBManager.getIns(APP.getContext()).queryAllStuffs(type);

        int fetched = 0;
        try {
            if (latest.isEmpty()) {
                fetched = fetchLatest();
                Log.d(TAG, "no latest, fresh fetch");
            } else if (ACTION_FETCH_REFRESH.equals(intent.getAction())) {
                Log.d(TAG, "latest fetch: " + latest.get(0).getPublishedAt());
                fetched = fetchRefresh(latest.get(0).getPublishedAt());
            } else if (ACTION_FETCH_MORE.equals(intent.getAction())) {
                Log.d(TAG, "earliest fetch: " + latest.get(latest.size() - 1).getPublishedAt());
                fetched = fetchMore(latest.get(latest.size() - 1).getPublishedAt());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendResult(intent, fetched);
    }

    private void sendResult(Intent intent, int fetched) {
        Log.d(TAG, "finished fetching, actual fetched " + fetched);

        Intent broadcast = new Intent(ACTION_UPDATE_RESULT);
        broadcast.putExtra(EXTRA_FETCHED, fetched);
        broadcast.putExtra(EXTRA_TRIGGER, intent.getAction());
        broadcast.putExtra(EXTRA_TYPE, type);

        localBroadcastManager.sendBroadcast(broadcast);
    }


    private int fetchLatest() throws InterruptedException, ExecutionException, ParseException, JSONException {
        RequestFuture<JSONObject> future = VolleyUtil.getInstance(this).getJSONSync(latestUrl, type);

        int fetched = 0;
        JSONObject response = future.get();
        if (response.getBoolean("error"))
            return 0;

        JSONArray array = response.getJSONArray("results");
        int len = array.length();
        JSONObject androidObj;
        String url, date, id, author, title, type;
        Stuff stuff;
        for (int i = 0; i < len; i++) {
            androidObj = array.getJSONObject(i);
            url = androidObj.getString("url");
            date = androidObj.getString("publishedAt");
            id = androidObj.getString("_id");
            author = androidObj.getString("who");
            title = androidObj.getString("desc");
            type = Constants.handleTypeStr(androidObj.getString("type"));

            stuff = new Stuff(id, type, title, url, author, DateUtil.parse(date), new Date(), false);
            if (!saveToDb(stuff)) {
                return i;
            }
            fetched++;
        }

        return fetched;
    }

    private int fetchRefresh(Date publishedAt) throws InterruptedException, ExecutionException, ParseException, JSONException {
        String after = DateUtil.format(publishedAt);
        List<String> dates = DateUtil.generateSequenceDateTillToday(publishedAt);
        return fetch(after, dates);
    }

    private int fetchMore(Date publishedAt) throws InterruptedException, ExecutionException, JSONException, ParseException {
        String before = DateUtil.format(publishedAt);
        List<String> dates = DateUtil.generateSequenceDateBefore(publishedAt, 10);
        return fetch(before, dates);
    }

    private int fetch(String after, List<String> dates) throws InterruptedException, ExecutionException, JSONException, ParseException {
        int fetched = 0;
        for (String date : dates) {
            if (date == null)
                return fetched;

            if (date.equals(after))
                continue;

            RequestFuture<JSONObject> stuffFuture = VolleyUtil.getInstance(this).getJSONSync(Constants.DAYLY_DATA_URL + date, type);

            JSONObject imgResponse = stuffFuture.get();
            if (imgResponse.getBoolean("error"))
                continue;

            JSONObject results = imgResponse.getJSONObject("results");
            if (!results.has(typeName))
                continue;

            JSONArray stuffs = results.getJSONArray(typeName);
            int len = stuffs.length();
            for (int i = 0; i < len; i++) {
                JSONObject stuff = stuffs.getJSONObject(i);
                if (stuff == null)
                    continue;

                if (!saveToDb(new Stuff(
                        stuff.getString("_id"),
                        Constants.handleTypeStr(stuff.getString("type")),
                        stuff.getString("desc"),
                        stuff.getString("url"),
                        stuff.getString("who"),
                        DateUtil.parse(stuff.getString("publishedAt")),
                        new Date(),
                        false
                ))) {
                    return fetched;
                }
                fetched++;
            }

        }
        return fetched;
    }

    private boolean saveToDb(Stuff stuff) {
        DBManager.getIns(APP.getContext()).insertStuff(stuff);
        return true;
    }
}
