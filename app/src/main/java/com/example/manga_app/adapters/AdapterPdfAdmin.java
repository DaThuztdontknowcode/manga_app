package com.example.manga_app.adapters;

import static com.example.manga_app.Constants.MAX_BYTES_PDF;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manga_app.MyApplication;
import com.example.manga_app.databinding.RowPdfAdminBinding;
import com.example.manga_app.filters.FilterPdfAdmin;
import com.example.manga_app.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {
    private Context context;
    public ArrayList<ModelPdf> pdfArrayList,filterList;
    private RowPdfAdminBinding binding;
    private FilterPdfAdmin filter;
    private static final String TAG ="PDF_ADAPTER_TAG";
    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        ModelPdf model = pdfArrayList.get(position);
        String description = model.getDescription();
        String title  = model.getTitle();
        String timestamp = model.getTimestamp();
        String formattedDate = MyApplication.formatTimestamp(String.valueOf(timestamp));

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        LoadCategory(model, holder);
        LoadPdfFromUrl(model, holder);
        LoadPdfSize(model, holder);

    }

    private void LoadPdfSize(ModelPdf model, HolderPdfAdmin holder) {
        String pdfUrl = model.getUrl();
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG,"onSuccess:"+model.getTitle()+""+bytes);
                        double kb= bytes/1024;
                        double mb=kb/1024;
                        if (mb >= 1) {
                            holder.sizeTv.setText(String.format("%.2f", mb) + " MB");
                        }
                        else if (kb >= 1) {
                            holder.sizeTv.setText(String.format("%.2f", kb) + " KB");
                        }
                        else {
                            holder.sizeTv.setText(String.format("%.2f",bytes)+"bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure (@NonNull Exception e) {
                Log.d(TAG,"onFailure:"+e.getMessage());
            }
            });
        }

    private void LoadPdfFromUrl(ModelPdf model, HolderPdfAdmin holder) {
        String pdfUrl = model.getUrl();
        StorageReference ref=FirebaseStorage.getInstance().getReferenceFromUrl (pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG, "onSuccess" + model.getTitle() + "successfully got the file PDF");
                holder.pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError(new OnErrorListener() {
                            @Override
                            public void onError(Throwable t) {
                                holder.progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "onError: " + t.getMessage());
                            }
                        })
                        .onPageError(new OnPageErrorListener() {
                            @Override
                            public void onPageError(int page, Throwable t) {
                                holder.progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "onPageError: " + t.getMessage());
                            }
                        })
                        .onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {
                                holder.progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "LoadComplete:PDF loaded " );
                            }
                        })
                        .load();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                holder.progressBar.setVisibility(View.INVISIBLE);
                Log.d(TAG,"onFailure"+model.getTitle()+"Failed to got the file PDF");
            }
        });
    }

    private void LoadCategory(ModelPdf model, HolderPdfAdmin holder) {
        String categoryId= model.getCategoryId();
        DatabaseReference ref=  FirebaseDatabase.getInstance().getReference( "Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category= ""+snapshot.child("category").getValue();
                        holder.categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter== null){
            filter = new FilterPdfAdmin(filterList,  this);}
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        public HolderPdfAdmin (@NonNull View itemView) {
            super(itemView);
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
        }
    }
}
