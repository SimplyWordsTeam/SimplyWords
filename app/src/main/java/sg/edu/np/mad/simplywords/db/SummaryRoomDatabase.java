package sg.edu.np.mad.simplywords.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sg.edu.np.mad.simplywords.dao.SummaryDao;
import sg.edu.np.mad.simplywords.model.Summary;

@Database(entities = {Summary.class}, version = 2, exportSchema = false)
public abstract class SummaryRoomDatabase extends RoomDatabase {
    private static final int THREAD_COUNT = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(THREAD_COUNT);
    private static volatile SummaryRoomDatabase INSTANCE;

    public static SummaryRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SummaryRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SummaryRoomDatabase.class, "summary_database").fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract SummaryDao summaryDao();
}
