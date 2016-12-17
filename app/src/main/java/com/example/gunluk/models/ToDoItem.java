package com.example.gunluk.models;

import java.util.Date;

/**
 * Represents an item in a ToDo list
 */
public class ToDoItem {
    @com.google.gson.annotations.SerializedName("text")
    private String mText;
    @com.google.gson.annotations.SerializedName("id")
    private String mId;
    @com.google.gson.annotations.SerializedName("complete")
    private boolean mComplete;
    @com.google.gson.annotations.SerializedName("state")
    private String mState;
    @com.google.gson.annotations.SerializedName("diary")
    private String mDiary;
    @com.google.gson.annotations.SerializedName("userId")
    private String mUserId;
    @com.google.gson.annotations.SerializedName("diaryImage")
    private String mDiaryImage;

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(Date mCreatedAt) {
        this.mCreatedAt = mCreatedAt;
    }

    @com.google.gson.annotations.SerializedName("createdAt")
    private Date mCreatedAt;


    /**
     * ToDoItem constructor
     */
    public ToDoItem() {}
    @Override
    public String toString() {
        return getText();
    }

    /**
     * Initializes a new ToDoItem
     * @param text
     *            The item text
     * @param id
     *            The item id
     * @param state
     *            The item id
     * @param diary
     *            The item id*
     * @param userId
     *            The item id
     */
    public ToDoItem(String text, String id,String state,String diary,String userId,String diaryImage) {
        this.setText(text);
        this.setId(id);
        this.setState (state);
        this.setDiary (diary);
        this.setUserId (userId);
        this.setText(text);
        this.setDiaryImage (diaryImage);

    }
    public String getDiaryImage(){
    return mDiaryImage;
}
    public String getState() {
        return mState;
    }
    public String getDiary() {
        return mDiary;
    }
    public String getUserId() {
        return mUserId;
    }
    public String getText() {
        return mText;
    }
    /**
     * Sets the item text
     *
     * @param text
     *            text to set
     */
    public final void setText(String text) {
        mText = text;
    }
    /**
     * Returns the item id
     */
    public String getId() {
        return mId;
    }
    /**
     * Sets the item id
     *
     * @param id
     *            id to set
     */

    public final void setId(String id) {
        mId = id;
    }
    /**
     * Indicates if the item is marked as completed
     */

    public boolean isComplete() {
        return mComplete;
    }
    /**
     * Marks the item as completed or incompleted
     */
    public void  setDiaryImage(String diaryImage){
        mDiaryImage=diaryImage;
    }

    public void setComplete(boolean complete) {
        mComplete = complete;
    }
    public final void setState(String state) {
        mState =state;
    }
    public final void setDiary(String diary) {mDiary = diary;}
    public final void setUserId(String userId) {mUserId = userId;}
    @Override
    public boolean equals(Object o) {
        return o instanceof ToDoItem && ((ToDoItem) o).mId == mId;
    }

}