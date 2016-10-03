package com.keshavg.reddit.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.keshavg.reddit.R;
import com.keshavg.reddit.activities.CommentsActivity;
import com.keshavg.reddit.activities.SubmitCommentActivity;
import com.keshavg.reddit.db.AuthSharedPrefHelper;
import com.keshavg.reddit.models.Comment;
import com.keshavg.reddit.services.CommentService;
import com.keshavg.reddit.utils.Constants;
import com.keshavg.reddit.utils.ValidateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import static android.view.View.GONE;

/**
 * Created by keshav.g on 29/08/16.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>
        implements SectionTitleProvider {
    private Activity activity;
    private String url;
    private String sortByParam;
    private Boolean isProfileActivity;
    private Map<String, View> viewById;
    private Map<String, LinearLayout> childViewOfCommentById;
    private Map<String, ViewHolder> viewHolderById;
    private Map<String, Integer> adapterPositionById;
    private List<Comment> objects;
    private Boolean isCollapsed;
    private CoordinatorLayout coordinatorLayout;

    public CommentsAdapter(Activity activity, String url, String sortByParam, Boolean isProfileActivity) {
        this.activity = activity;
        this.url = url;
        this.sortByParam = sortByParam;
        this.isProfileActivity = isProfileActivity;

        viewById = new HashMap<>();
        childViewOfCommentById = new HashMap<>();
        viewHolderById = new HashMap<>();
        adapterPositionById = new HashMap<>();
        objects = new ArrayList<>();
        isCollapsed = false;

        coordinatorLayout = (CoordinatorLayout) activity.findViewById(R.id.coordinator_layout);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout comment;

        RelativeLayout header;
        ImageView collapse;
        TextView author;
        TextView created;
        TextView upvotes;

        TextView commentBody;
        Toolbar toolbar;
        ImageButton commentUpvote;
        ImageButton commentDownvote;
        ImageButton commentReply;
        ImageButton commentEdit;
        ImageButton commentDelete;
        MenuItem commentParent;
        MenuItem commentFullComments;

        LinearLayout subcommentsView;

        ProgressBar progressBar;
        Button button;

        public ViewHolder(View v) {
            super(v);

            this.comment = (LinearLayout) v.findViewById(R.id.comment);

            this.header = (RelativeLayout) this.comment.findViewById(R.id.comment_header);
            this.collapse = (ImageView) this.header.findViewById(R.id.comment_collapse);
            this.author = (TextView) this.header.findViewById(R.id.comment_author);
            this.created = (TextView) this.header.findViewById(R.id.comment_created);
            this.upvotes = (TextView) this.header.findViewById(R.id.comment_score);

            this.commentBody = (TextView) this.comment.findViewById(R.id.comment_body);
            this.toolbar = (Toolbar) this.comment.findViewById(R.id.toolbar);
            this.toolbar.inflateMenu(R.menu.comment_functions);
            this.commentUpvote = (ImageButton) this.toolbar.findViewById(R.id.comment_upvote);
            this.commentDownvote = (ImageButton) this.toolbar.findViewById(R.id.comment_downvote);
            this.commentReply = (ImageButton) this.toolbar.findViewById(R.id.comment_reply);
            this.commentEdit = (ImageButton) this.toolbar.findViewById(R.id.comment_edit);
            this.commentDelete = (ImageButton) this.toolbar.findViewById(R.id.comment_delete);
            this.commentParent = this.toolbar.getMenu().findItem(R.id.comment_parent);
            this.commentFullComments = this.toolbar.getMenu().findItem(R.id.comment_full);

            this.subcommentsView = (LinearLayout) v.findViewById(R.id.subcomments_list);

            this.progressBar = (ProgressBar) v.findViewById(R.id.progressbar);
            this.button = (Button) v.findViewById(R.id.button);
        }
    }

    @Override
    public String getSectionTitle(int position) {
        return String.valueOf(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.comment_row, parent, false);

        makeRandomColorLine(view);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final Comment comment = objects.get(position);
        adapterPositionById.put(comment.getName(), position);
        setViewData(viewHolder, comment);
    }

    private void setViewData(final ViewHolder viewHolder, final Comment comment) {
        viewById.put(comment.getName(), viewHolder.comment);
        viewHolderById.put(comment.getName(), viewHolder);

        viewHolder.author.setText(comment.getPostedBy());

        if (comment.getHtmlBody() != null) {
            viewHolder.commentBody.setText(Html.fromHtml(comment.getHtmlBody()));
        }

        viewHolder.created.setText(comment.getRelativeTime());

        viewHolder.toolbar.setVisibility(GONE);
        if (isProfileActivity) {
            viewHolder.commentReply.setVisibility(GONE);
            viewHolder.commentFullComments.setVisible(true);
        } else {
            viewHolder.commentReply.setVisibility(View.VISIBLE);
            viewHolder.commentFullComments.setVisible(false);
        }

        setScoreInformation(viewHolder, comment);

        if (!childViewOfCommentById.containsKey(comment.getName())) {
            // do not repeat this on notifydatasetchanged
            createThreadedComments(viewHolder.subcommentsView, comment.getReplies());
        }
        childViewOfCommentById.put(comment.getName(), viewHolder.subcommentsView);

        viewHolder.header.setOnClickListener(v -> onClickCommentCollapse(viewHolder, comment.getMoreReplyIds() != null));

        viewHolder.commentBody.setOnClickListener(v -> onClickComment(viewHolder));

        viewHolder.commentUpvote.setOnClickListener(v -> onClickVote(comment, 1, viewHolder));

        viewHolder.commentDownvote.setOnClickListener(v -> onClickVote(comment, -1, viewHolder));

        viewHolder.commentReply.setOnClickListener(v -> onClickReply(comment.getName()));

        if (AuthSharedPrefHelper.isLoggedIn()) {
            if (comment.getAuthor().equals(AuthSharedPrefHelper.getUsername())) {
                viewHolder.commentEdit.setVisibility(View.VISIBLE);
                viewHolder.commentDelete.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.commentEdit.setVisibility(GONE);
            viewHolder.commentDelete.setVisibility(GONE);
        }

        viewHolder.commentEdit.setOnClickListener(v -> {
            Intent i = new Intent(activity, SubmitCommentActivity.class);
            i.putExtra(SubmitCommentActivity.ID, comment.getName());
            i.putExtra(SubmitCommentActivity.COMMENT, comment.getBody());
            activity.startActivityForResult(i, CommentsActivity.COMMENT_SUBMIT_REQUEST_CODE);
        });

        viewHolder.commentDelete.setOnClickListener(v -> onClickDelete(viewHolder, comment.getName()));

        if (comment.getParentId().startsWith(Constants.POST_PREFIX)) { // no parent for direct comments to post
            viewHolder.commentParent.setVisible(false);
        } else {
            viewHolder.commentParent.setVisible(true);
        }
        viewHolder.commentParent.setOnMenuItemClickListener(item -> {
            View targetView = viewById.get(comment.getParentId());
            targetView.getParent().requestChildFocus(targetView, targetView);
            return true;
        });

        viewHolder.commentFullComments.setOnMenuItemClickListener(item -> {
            Intent i = new Intent(activity, CommentsActivity.class);
            i.putExtra(CommentsActivity.TITLE, comment.getPostTitle());
            i.putExtra(CommentsActivity.URL, comment.getFullCommentLink());
            activity.startActivity(i);

            return true;
        });

        final Queue<String> moreIds = comment.getMoreReplyIds();
        if (moreIds != null && !moreIds.isEmpty()) {
            viewHolder.button.setVisibility(View.VISIBLE);
            viewHolder.button.setOnClickListener((v) -> onClickLoadMore(viewHolder, moreIds));
        }
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }


    /**
     * Function to clear the contents of the adapter
     * And update the view
     */
    public void clear() {
        this.objects.clear();
        notifyDataSetChanged();
    }

    /**
     * Function to add object to the adapter
     * And update the view
     * @param index
     * @param comment
     */
    public void add(int index, Comment comment) {
        this.objects.add(index, comment);
        notifyDataSetChanged();
    }

    /**
     *
     * Function to add a single reply to a comment
     * @param parentId
     * @param comment
     */
    public void add(String parentId, Comment comment) {
        View view = LayoutInflater.from(activity).inflate(R.layout.comment_row, null);
        makeRandomColorLine(view);
        setViewData(new ViewHolder(view), comment);
        childViewOfCommentById.get(parentId).addView(view, 0);
    }

    /**
     * Function to add objects to the adapter
     * And update the view
     * @param objects
     */
    public void addAll(List<Comment> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
    }

    public void edit(String id, Comment comment) {
        if (comment.getParentId().startsWith(Constants.POST_PREFIX)) {
            this.objects.get(adapterPositionById.get(id)).setData(comment);
        }

        setViewData(viewHolderById.get(id), comment);
    }

    private void setScoreInformation(ViewHolder viewHolder, Comment comment) {
        viewHolder.upvotes.setText(comment.getScoreString());
        if (comment.getLikeInt() == 1) {
            viewHolder.commentUpvote.setColorFilter(activity.getColor(R.color.colorAccent));
            viewHolder.commentDownvote.setColorFilter(activity.getColor(android.R.color.black));
        } else if (comment.getLikeInt() == -1) {
            viewHolder.commentUpvote.setColorFilter(activity.getColor(android.R.color.black));
            viewHolder.commentDownvote.setColorFilter(activity.getColor(R.color.colorAccent));
        } else {
            viewHolder.commentUpvote.setColorFilter(activity.getColor(android.R.color.black));
            viewHolder.commentDownvote.setColorFilter(activity.getColor(android.R.color.black));
        }
    }

    private void createThreadedComments(LinearLayout subcomments, List<Comment> replies) {
        if (replies != null && replies.size() > 0) {
            for (final Comment reply : replies) {
                View view = LayoutInflater.from(activity).inflate(R.layout.comment_row, subcomments, false);
                makeRandomColorLine(view);
                setViewData(new ViewHolder(view), reply);
                subcomments.addView(view);
            }
        }
    }

    /**
     *
     * @param viewHolder
     * @param isPresentMoreIds
     */
    private void onClickCommentCollapse(ViewHolder viewHolder, Boolean isPresentMoreIds) {
        if (!isCollapsed) {
            viewHolder.button.setVisibility(GONE);
            viewHolder.subcommentsView.setVisibility(GONE);
            viewHolder.toolbar.setVisibility(GONE);
            viewHolder.commentBody.setVisibility(GONE);

            viewHolder.collapse.setImageDrawable(activity.getDrawable(R.drawable.ic_keyboard_arrow_right));
        } else {
            viewHolder.commentBody.setVisibility(View.VISIBLE);
            viewHolder.subcommentsView.setVisibility(View.VISIBLE);

            if (isPresentMoreIds) {
                viewHolder.button.setVisibility(View.VISIBLE);
            }

            viewHolder.collapse.setImageDrawable(activity.getDrawable(R.drawable.ic_keyboard_arrow_down));
        }

        isCollapsed = !isCollapsed;
    }

    /**
     * On click listener on the comment
     * Opens up a menu to perform various actions
     * @param viewHolder
     */
    private void onClickComment(ViewHolder viewHolder) {
        if (viewHolder.toolbar.getVisibility() == View.VISIBLE) {
            viewHolder.toolbar.setVisibility(GONE);
        } else {
            viewHolder.toolbar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * On click listener for up voting or down voting
     * @param comment
     * @param likes
     * @param viewHolder
     */
    private void onClickVote(final Comment comment, int likes, final ViewHolder viewHolder) {
        if (ValidateUtil.loginValidation(coordinatorLayout, activity)) {

            final int prevLikes = comment.getLikeInt();
            comment.setLikes((comment.getLikeInt() == likes) ? 0 : likes);
            final int delta = comment.getLikeInt() - prevLikes;
            comment.updateScore(delta);
            setScoreInformation(viewHolder, comment);

            CommentService.voteComment(
                    activity.getApplicationContext(),
                    comment,
                    () -> {
                        comment.setLikes(prevLikes);
                        comment.updateScore(-delta);
                        setScoreInformation(viewHolder, comment);
                    }
            );
        }
    }

    private void onClickReply(final String parentId) {
        if (ValidateUtil.loginValidation(coordinatorLayout, activity)) {
            Intent i = new Intent(activity, SubmitCommentActivity.class);
            i.putExtra(SubmitCommentActivity.PARENT_ID, parentId);
            activity.startActivityForResult(i, CommentsActivity.COMMENT_SUBMIT_REQUEST_CODE);
        }
    }

    /**
     * On click listener for the load more button
     * Fetches more comments from the REST api and adds to the adapter
     * @param viewHolder
     * @param moreIds
     */
    private void onClickLoadMore(final ViewHolder viewHolder, final Queue<String> moreIds) {
        viewHolder.button.setVisibility(GONE);
        viewHolder.progressBar.setVisibility(View.VISIBLE);

        final CommentService commentService = new CommentService();
        commentService.fetchCommentsForPosts(
                activity.getApplicationContext(),
                url,
                moreIds.peek(),
                sortByParam,
                () -> {
                    createThreadedComments(viewHolder.subcommentsView, commentService.getCommentResponse().getComments());

                    viewHolder.progressBar.setVisibility(GONE);
                    moreIds.remove();
                    if (moreIds != null && !moreIds.isEmpty()) {
                        viewHolder.button.setVisibility(View.VISIBLE);
                    }
                },
                () -> {
                    viewHolder.progressBar.setVisibility(GONE);
                    viewHolder.button.setVisibility(View.VISIBLE);
                }
        );
    }

    private void onClickDelete(final ViewHolder viewHolder, final String id) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle("Confirm Delete?")
                .setPositiveButton("Yes", ((dialog, which) -> deleteComment(viewHolder, id)))
                .setNegativeButton("No", ((dialog, which) -> dialog.cancel()))
                .create();
        alertDialog.show();
    }

    private void deleteComment(final ViewHolder viewHolder, String id) {
        CommentService.deleteComment(
                activity.getApplicationContext(),
                id,
                () -> {
                    viewHolder.commentBody.setText("[removed]");
                    viewHolder.author.setText("[deleted]");
                }
        );
    }

    /**
     * Generate a random background color for the comment start line
     * @param convertView
     */
    private void makeRandomColorLine(View convertView) {
        Random rnd = new Random();
        int color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        convertView.findViewById(R.id.comment_start_line).setBackgroundColor(color);
    }
}