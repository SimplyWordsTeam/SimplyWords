package sg.edu.np.mad.simplywords.repo;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import sg.edu.np.mad.simplywords.dao.SummaryDao;
import sg.edu.np.mad.simplywords.db.SummaryRoomDatabase;
import sg.edu.np.mad.simplywords.model.Summary;

public class SummaryRepository {
    private final SummaryDao mSummaryDao;
    private final LiveData<List<Summary>> mAllSummaries;

    public SummaryRepository(Application app) {
        SummaryRoomDatabase db = SummaryRoomDatabase.getDatabase(app);
        mSummaryDao = db.summaryDao();
        mAllSummaries = mSummaryDao.getAllSummaries();
    }

    public LiveData<List<Summary>> getAllSummaries() {
        return mAllSummaries;
    }

    public void insertSummaries(Summary... summaries) {
        SummaryRoomDatabase.databaseWriteExecutor.execute(() -> {
            mSummaryDao.insertSummaries(summaries);
        });
    }

    public void deleteSummaries(Summary... summaries) {
        SummaryRoomDatabase.databaseWriteExecutor.execute(() -> {
            mSummaryDao.deleteSummaries(summaries);
        });
    }

    public LiveData<Summary> getSummaryById(int id) {
        return mSummaryDao.getSummaryById(id);
    }
}
