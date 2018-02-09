package org.fao.sola.clients.android.opentenure;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.fao.sola.clients.android.opentenure.model.Boundary;

public class BoundaryListItemViewHolder {
    TextView name;
    ProgressBar bar;
    ImageView processed;
    ImageView send;
    ImageView delete;
    Boundary boundary;

    public TextView getName() {
        return name;
    }

    public void setName(TextView name) {
        this.name = name;
    }

    public ProgressBar getBar() {
        return bar;
    }

    public void setBar(ProgressBar bar) {
        this.bar = bar;
    }

    public ImageView getProcessed() {
        return processed;
    }

    public void setProcessed(ImageView processed) {
        this.processed = processed;
    }

    public ImageView getSend() {
        return send;
    }

    public void setSend(ImageView send) {
        this.send = send;
    }

    public Boundary getBoundary() {
        return boundary;
    }

    public void setBoundary(Boundary boundary) {
        this.boundary = boundary;
    }

    public ImageView getDelete() {
        return delete;
    }

    public void setDelete(ImageView delete) {
        this.delete = delete;
    }

    public BoundaryListItemViewHolder(){
    }
}
