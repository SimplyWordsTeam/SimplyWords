package sg.edu.np.mad.simplywords.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import sg.edu.np.mad.simplywords.model.Summary;

@Dao
public interface SummaryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertSummaries(Summary... summaries);

    @Delete
    void deleteSummaries(Summary... summaries);

    @Query("SELECT * FROM summary ORDER BY created_at DESC")
    LiveData<List<Summary>> getAllSummaries();

    @Query("SELECT * FROM summary WHERE id = :id")
    Summary getSummaryById(int id);
}
