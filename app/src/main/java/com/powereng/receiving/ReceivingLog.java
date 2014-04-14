package com.powereng.receiving;

/**
 * Created by Logjammin on 4/13/14.
 */
public class ReceivingLog {
    private long id;

    private String sender;
    private String recipient;
    private String sig;
    private String PO;
    private String pcs;
    private String carrier;
    private String date;
    private String tracking;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSig() {
        return sig;
    }

    public String getPO() {
        return PO;
    }

    public String getPcs() {
        return pcs;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getDate() {
        return date;
    }

    public String getTracking() {
        return tracking;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public void setPO(String PO) {
        this.PO = PO;
    }

    public void setPcs(String pcs) {
        this.pcs = pcs;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTracking(String tracking) {
        this.tracking = tracking;
    }
}
