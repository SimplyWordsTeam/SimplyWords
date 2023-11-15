package sg.edu.np.mad.simplywords.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import sg.edu.np.mad.simplywords.model.Summary;
import sg.edu.np.mad.simplywords.repo.SummaryRepository;

public class SummaryViewModel extends AndroidViewModel {

    private final SummaryRepository mRepository;
    private final LiveData<List<Summary>> mAllSummaries;

    public SummaryViewModel(@NonNull Application application) {
        super(application);
        mRepository = new SummaryRepository(application);
        mAllSummaries = mRepository.getAllSummaries();
    }

    public LiveData<List<Summary>> getAllSummaries() {
        return mAllSummaries;
    }

    public void insertSummaries(Summary summary) {
        mRepository.insertSummaries(summary);
    }

    public void deleteSummaries(Summary summary) {
        mRepository.deleteSummaries(summary);
    }

    public void getSummaryById(Integer id) {
        mRepository.getSummaryById(id);
    }
}
