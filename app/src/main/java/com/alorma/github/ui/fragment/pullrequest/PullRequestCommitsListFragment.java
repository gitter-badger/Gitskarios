package com.alorma.github.ui.fragment.pullrequest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.alorma.github.R;
import com.alorma.github.sdk.bean.dto.response.Commit;
import com.alorma.github.sdk.bean.info.IssueInfo;
import com.alorma.github.sdk.services.pullrequest.GetPullRequestCommits;
import com.alorma.github.ui.adapter.commit.CommitsAdapter;
import com.alorma.github.ui.fragment.base.PaginatedListFragment;
import com.alorma.github.ui.fragment.detail.repo.BackManager;
import com.alorma.github.ui.fragment.detail.repo.PermissionsManager;
import com.mikepenz.octicons_typeface_library.Octicons;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by Bernat on 07/09/2014.
 */
public class PullRequestCommitsListFragment extends PaginatedListFragment<List<Commit>, CommitsAdapter> implements PermissionsManager
        , BackManager {

    private static final String ISSUE_INFO = "ISSUE_INFO";

    private List<Commit> commits;

    private IssueInfo issueInfo;

    public static PullRequestCommitsListFragment newInstance(IssueInfo issueInfo) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ISSUE_INFO, issueInfo);

        PullRequestCommitsListFragment fragment = new PullRequestCommitsListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void onResponse(final List<Commit> commits, boolean refreshing) {
        if (this.commits == null || refreshing) {
            this.commits = new ArrayList<>();
        }
        if (commits != null && commits.size() > 0) {
            orderCommits(commits);

            if (getAdapter() == null ) {
                CommitsAdapter commitsAdapter = new CommitsAdapter(LayoutInflater.from(getActivity()), false);
                commitsAdapter.addAll(PullRequestCommitsListFragment.this.commits);
                setAdapter(commitsAdapter);
            } else {
                getAdapter().addAll(commits);
            }
        } else if (getAdapter() == null || getAdapter().getItemCount() == 0) {
            setEmpty();
        }
    }

    @Override
    public void onFail(RetrofitError error) {
        super.onFail(error);
        if (getAdapter() == null || getAdapter().getItemCount() == 0) {
            if (error != null && error.getResponse() != null) {
                setEmpty(error.getResponse().getStatus());
            }
        }
    }

    @Override
    public void setEmpty(int statusCode) {
        super.setEmpty(statusCode);
        if (fab != null) {
            fab.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void hideEmpty() {
        super.hideEmpty();
        if (fab != null) {
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setPermissions(boolean admin, boolean push, boolean pull) {

    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    private void orderCommits(List<Commit> commits) {

        for (Commit commit : commits) {
            if (commit.commit.author.date != null) {
                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                DateTime dt = formatter.parseDateTime(commit.commit.committer.date);

                Days days = Days.daysBetween(dt.withTimeAtStartOfDay(), new DateTime(System.currentTimeMillis()).withTimeAtStartOfDay());

                commit.days = days.getDays();

                this.commits.add(commit);
            }
        }
    }

    @Override
    protected void executeRequest() {
        super.executeRequest();
        GetPullRequestCommits getPullRequestCommits = new GetPullRequestCommits(getActivity(), issueInfo);
        getPullRequestCommits.setOnResultCallback(this);
        getPullRequestCommits.execute();
    }

    @Override
    protected void executePaginatedRequest(int page) {
        super.executePaginatedRequest(page);
        GetPullRequestCommits getPullRequestCommits = new GetPullRequestCommits(getActivity(), issueInfo);
        getPullRequestCommits.setOnResultCallback(this);
        getPullRequestCommits.execute();
    }

    @Override
    protected void loadArguments() {
        if (getArguments() != null) {
            issueInfo = getArguments().getParcelable(ISSUE_INFO);
        }
    }

    @Override
    protected Octicons.Icon getNoDataIcon() {
        return Octicons.Icon.oct_diff;
    }

    @Override
    protected int getNoDataText() {
        return R.string.no_commits;
    }

    @Override
    protected boolean useFAB() {
        return false;
    }

    // TODO

    /*@Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        Commit item = commitsAdapter.getItem(position);

        CommitInfo info = new CommitInfo();
        info.repoInfo = issueInfo.repoInfo;
        info.sha = item.sha;

        Intent intent = CommitDetailActivity.launchIntent(getActivity(), info);
        startActivity(intent);
    }*/
}
