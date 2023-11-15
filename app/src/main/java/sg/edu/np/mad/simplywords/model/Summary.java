package sg.edu.np.mad.simplywords.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Summary {
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "original_text")
    public String originalText;

    @ColumnInfo(name = "summarized_text")
    public String summarizedText;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public Summary(String originalText, String summarizedText) {
        this.originalText = originalText;
        this.summarizedText = summarizedText;
        this.createdAt = System.currentTimeMillis();
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getSummarizedText() {
        return summarizedText;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
