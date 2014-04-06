package jobs.facebook;

import helpers.FileHelper;

import java.io.InputStream;
import java.util.List;

import models.FacebookFriend;
import models.FacebookUser;
import models.User;
import play.Logger;
import play.jobs.Job;
import play.libs.WS;

public class RefreshBeansightAvatarWithFacebookImageJob extends Job {

	public Long currentUserId = null;
	
    public RefreshBeansightAvatarWithFacebookImageJob(
			Long currentUserId) {
		super();
		this.currentUserId = currentUserId;
	}


	@Override
	public void doJob() throws Exception {
		if (currentUserId == null) {
			return;
		}
		
		User currentUser = User.findById(currentUserId);
		
    	if (!currentUser.avatarSmall.exists()) {
    		try {
				InputStream profileImageInputStream = WS.url("http://graph.facebook.com/%s/picture?type=large", currentUser.facebookUserId.toString()).get().getStream();
				// and now we can update avatar with the facebook profil image
				currentUser.updateAvatar(
						FileHelper.getTmpFile(profileImageInputStream), true);
			} catch (Exception e) {
				Logger.error("cannot get user's Facebook image : %s", e.getMessage());
			}
    	}
	}
	
}
