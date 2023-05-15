package com.android.campusmoments.Service;

import android.app.Person;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.campusmoments.Activity.AvatarConfigActivity;
import com.android.campusmoments.Activity.BioConfigActivity;
import com.android.campusmoments.Activity.LoginActivity;
import com.android.campusmoments.Activity.MainActivity;
import com.android.campusmoments.Activity.PasswordConfigActivity;
import com.android.campusmoments.Activity.PersonCenterActivity;
import com.android.campusmoments.Activity.RegisterActivity;
import com.android.campusmoments.Activity.UsernameConfigActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Services {

    private static final String TAG = "Services";
    private static final String BASE_URL = "http://10.0.2.2:8000/users/api/";
    private static final String LOGIN_URL = BASE_URL + "login";
    private static final String REGISTER_URL = BASE_URL + "register";
    private static final String SELF_URL = BASE_URL + "self";
    private static final String PATCH_USER_URL = BASE_URL + "users/";
    private static final String LOGOUT_URL = BASE_URL + "logout";
    public static String token = null;
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType MEDIA_TYPE_FORM_DATA = MediaType.parse("multipart/form-data; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();
    private static Handler tokenHandler = null;
    private static Handler loginHandler = null;
    private static Handler registerHandler = null;
    private static Handler setAvatarHandler = null;
    private static Handler setUsernameHandler = null;
    private static Handler setBioHandler = null;
    private static Handler setPasswordHandler = null;
    private static Handler logoutHandler = null;

    public static User mySelf = null;
    public static void setLoginHandler(Handler _loginHandler) {
        loginHandler = _loginHandler;
    }
    public static void setRegisterHandler(Handler _registerHandler) {
        registerHandler = _registerHandler;
    }
    public static void setSetAvatarHandler(Handler _setAvatarHandler) {
        setAvatarHandler = _setAvatarHandler;
    }
    public static void setSetUsernameHandler(Handler _setUsernameHandler) {
        setUsernameHandler = _setUsernameHandler;
    }
    public static void setTokenHandler(Handler _tokenHandler) {
        tokenHandler = _tokenHandler;
    }
    public static void setSetBioHandler(Handler _setBioHandler) {
        setBioHandler = _setBioHandler;
    }
    public static void setSetPasswordHandler(Handler _setPasswordHandler) {
        setPasswordHandler = _setPasswordHandler;
    }
    public static void setLogoutHandler(Handler _logoutHandler) {
        logoutHandler = _logoutHandler;
    }


    public static void tokenCheck(String token) {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Token " + token)
                .url(SELF_URL)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.code() != 200) {
                    return;
                }
                assert response.body() != null;
                String json = response.body().string();
                Log.d(TAG, "onResponse: " + json);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    mySelf = new User(jsonObject);
                    Message message = new Message();
                    message.what = MainActivity.TOKEN_VALID;
                    tokenHandler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = MainActivity.TOKEN_INVALID;
                    tokenHandler.sendMessage(message);
                }
            }
        });
    }
    public static void login(String username, String password) {
        String params = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        Log.d(TAG, "run: " + params);
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(okhttp3.RequestBody.create(MEDIA_TYPE_JSON, params))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                Message message = new Message();
                message.what = LoginActivity.LOGIN_FAIL;
                loginHandler.sendMessage(message);
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {

                if (response.code() != 200) {
                    Message message = new Message();
                    message.what = LoginActivity.LOGIN_FAIL;
                    loginHandler.sendMessage(message);
                    return;
                }
                Message message = new Message();
                message.what = LoginActivity.LOGIN_SUCCESS;
                assert response.body() != null;
                message.obj = response.body().string();
                Log.d(TAG, "onResponse: " + message.obj);
                loginHandler.sendMessage(message);
            }
        });
    }

    public static void register(String username, String password) {
        String params = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        Log.d(TAG, "run: " + params);
        Request request = new Request.Builder()
                .url(REGISTER_URL)
                .post(okhttp3.RequestBody.create(MEDIA_TYPE_JSON, params))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                Message message = new Message();
                message.what = RegisterActivity.REGISTER_FAIL;
                registerHandler.sendMessage(message);
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                Log.d(TAG, "onResponse: " + response.code());
                if (response.code() != 201) {
                    Message message = new Message();
                    message.what = RegisterActivity.REGISTER_FAIL;
                    registerHandler.sendMessage(message);
                    return;
                }
                Message message = new Message();
                message.what = RegisterActivity.REGISTER_SUCCESS;
                message.obj = response.body().string();
                registerHandler.sendMessage(message);
            }
        });
    }
    public static void getSelf(String token) {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Token " + token)
                .url(SELF_URL)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.code() != 200) {
                    return;
                }
                assert response.body() != null;
                String json = response.body().string();
                Log.d(TAG, "onResponse: " + json);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    mySelf = new User(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public static void setAvatar(String avatarPath) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        File file = new File(avatarPath);
        builder.addFormDataPart("avatar", file.getName(), RequestBody.create(MEDIA_TYPE_FORM_DATA, file));
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url(PATCH_USER_URL + mySelf.id)
                .addHeader("Authorization", "Token " + token)
                .patch(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                Message message = new Message();
                message.what = AvatarConfigActivity.SET_AVATAR_FAIL;
                setAvatarHandler.sendMessage(message);

            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.code() != 200) {
                    Message message = new Message();
                    message.what = AvatarConfigActivity.SET_AVATAR_FAIL;
                    setAvatarHandler.sendMessage(message);
                    return;
                }
                mySelf.avatar = avatarPath;
                Message message = new Message();
                message.what = AvatarConfigActivity.SET_AVATAR_SUCCESS;
                message.obj = response.body().string();
                Log.d(TAG, "onResponse: " + message.obj);
                setAvatarHandler.sendMessage(message);
            }
        });
    }

    public static void setUsername(String username) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        builder.addFormDataPart("username", username);
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .addHeader("Authorization", "Token " + token)
                .url(PATCH_USER_URL + mySelf.id)
                .patch(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                Message message = new Message();
                message.what = UsernameConfigActivity.SET_USERNAME_FAIL;
                setUsernameHandler.sendMessage(message);
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.code() != 200) {
                    Message message = new Message();
                    message.what = UsernameConfigActivity.SET_USERNAME_FAIL;
                    setUsernameHandler.sendMessage(message);
                    return;
                }
                mySelf.username = username;
                Message message = new Message();
                message.what = UsernameConfigActivity.SET_USERNAME_SUCCESS;
                message.obj = response.body().string();
                Log.d(TAG, "onResponse: " + message.obj);
                setUsernameHandler.sendMessage(message);
            }
        });
    }


    public static void setPassword(String password, String new_password) {
        // 这个不对
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        builder.addFormDataPart("password", password);
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .addHeader("Authorization", "Token " + token)
                .url(PATCH_USER_URL + mySelf.id)
                .patch(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                Message message = new Message();
                message.what = PasswordConfigActivity.SET_PASSWORD_FAIL;
                setPasswordHandler.sendMessage(message);
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.code() != 200) {
                    Message message = new Message();
                    message.what = PasswordConfigActivity.SET_PASSWORD_FAIL;
                    setPasswordHandler.sendMessage(message);
                    return;
                }
                Message message = new Message();
                message.what = PasswordConfigActivity.SET_PASSWORD_SUCCESS;
                message.obj = response.body().string();
                Log.d(TAG, "onResponse: " + message.obj);
                setPasswordHandler.sendMessage(message);
            }
        });
    }

    public static void setBio(String bio) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        builder.addFormDataPart("bio", bio);
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .addHeader("Authorization", "Token " + token)
                .url(PATCH_USER_URL + mySelf.id)
                .patch(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                Message message = new Message();
                message.what = BioConfigActivity.SET_BIO_FAIL;
                setBioHandler.sendMessage(message);
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.code() != 200) {
                    Message message = new Message();
                    message.what = BioConfigActivity.SET_BIO_FAIL;
                    setBioHandler.sendMessage(message);
                    return;
                }
                mySelf.bio = bio;
                Message message = new Message();
                message.what = BioConfigActivity.SET_BIO_SUCCESS;
                message.obj = response.body().string();
                Log.d(TAG, "onResponse: " + message.obj);
                setBioHandler.sendMessage(message);
            }
        });
    }

    public static void logout() {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Token " + token)
                .url(LOGOUT_URL)
                .post(okhttp3.RequestBody.create(null, new byte[0]))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
//                Message message = new Message();
//                message.what = LogoutActivity.LOGOUT_FAIL;
//                logoutHandler.sendMessage(message);
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.code() != 204) {
                    Message message = new Message();
                    message.what = PersonCenterActivity.LOGOUT_FAIL;
                    logoutHandler.sendMessage(message);
                    return;
                }
                Message message = new Message();
                message.what = PersonCenterActivity.LOGOUT_SUCCESS;
                logoutHandler.sendMessage(message);
            }
        });
    }
}
