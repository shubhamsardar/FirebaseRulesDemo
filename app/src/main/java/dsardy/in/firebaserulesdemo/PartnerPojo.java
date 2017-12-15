package dsardy.in.firebaserulesdemo;

import java.util.List;

/**
 * Created by Shubham on 12/7/2017.
 */

public class PartnerPojo {

    private String mName;
    private String mAge;
    private Boolean isPicsUploaded;
    private Boolean isVerified = false;
    private List<String> mList;

    public PartnerPojo(){
        //for firebase
    }

    public PartnerPojo(String mName, String mAge,List<String> mList) {
        this.mName = mName;
        this.mAge = mAge;
        this.mList = mList;
    }

    public List<String> getmList() {
        return mList;
    }

    public Boolean getPicsUploaded() {
        return isPicsUploaded;
    }

    public void setPicsUploaded(Boolean picsUploaded) {
        isPicsUploaded = picsUploaded;
    }

    public void setmList(List<String> mList) {
        this.mList = mList;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmAge() {
        return mAge;
    }

    public void setmAge(String mAge) {
        this.mAge = mAge;
    }

    public Boolean getVerified() {
        return isVerified;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }
}
