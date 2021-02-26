package com.javarticles.loanly;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;


//https://github.com/square/retrofit/issues/3033
//https://thanhtungvo.medium.com/upload-file-in-android-jave-with-retrofit-2-ae4822224e94
//https://www.learn2crack.com/2017/08/upload-image-using-retrofit.html
//https://www.baeldung.com/retrofit
//working = https://medium.com/android-news/working-with-retrofit-825d30348fe2
public interface UploadReceiptService{

    @Multipart
    @POST("/owner/")
    Call<FormData> uploadReceipt(
            @Header("Authorization") String Token,
            @Part MultipartBody.Part file,
            @Part ("name") RequestBody name,
            @Part ("mobile_no") RequestBody mobile_no,
            @Part ("Address") RequestBody Address,
            @Part ("adhar") RequestBody adhar

    );
}
