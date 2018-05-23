package com.github.freeacs.web.security;

import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.WebConstants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * The Class WebUser.
 */
public class WebUser extends User implements UserDetails {

	/** The authenticated. */
	private boolean authenticated = false;

	/**
	 * Make sure that the user always has access.
	 *
	 * @return the access
	 */
	@Override
	public String getAccess() {
		String access = super.getAccess();
		if (access == null)
			return Users.ACCESS_ADMIN;
		return access;
	}

	/**
	 * We cache allowed pages because there is no need to recalculate after the user has logged in.
	 *
	 * @return A list of page ids
	 */
	public List<String> getAllowedPages(String sessionId) {
		if (allowedPages == null) {
			String access = getAccess().split(";")[0];
			if (access.startsWith("WEB[") && access.endsWith("]")) {
				access = access.substring(access.indexOf("[") + 1);
				access = access.substring(0, access.length() - 1);
				List<String> arr = Arrays.asList(access.split(","));
				List<String> list = new ArrayList<String>(arr);
				List<Page> pages = Page.getPageValuesFromList(list);
				Page.addRequiredPages(pages);
				if (SessionCache.getSessionData(sessionId).getUser().isAdmin()) {
					pages.add(Page.PERMISSIONS);
				}
				list = Page.getStringValuesFromList(pages);
				allowedPages = list;
			} else {
				allowedPages = Arrays.asList(WebConstants.ALL_PAGES);
			}
		}
		return allowedPages;
	}

	/** The allowed pages. */
	private List<String> allowedPages;

	public WebUser(User user) {
		super(user);
		this.setSecretHashed(user.getSecret());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if (isAdmin()) {
			return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public String getPassword() {
		return this.getSecret();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
