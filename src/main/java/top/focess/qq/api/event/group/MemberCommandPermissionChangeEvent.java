package top.focess.qq.api.event.group;

import top.focess.command.CommandPermission;
import top.focess.qq.api.bot.contact.Member;
import top.focess.qq.api.event.Event;
import top.focess.qq.api.event.ListenerHandler;

/**
 * Called after member's permission changed
 */
public class MemberCommandPermissionChangeEvent extends Event {

	private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

	/**
	 * The member
	 */
	private final Member member;

	/**
	 * The old permission
	 */
	private final CommandPermission oldPermission;

	/**
	 * The new permission
	 */
	private final CommandPermission newPermission;

	/**
	 * Constructs a MemberCommandPermissionChangeEvent
	 * @param member the member
	 * @param oldPermission the old permission
	 * @param newPermission the new permission
	 */
	public MemberCommandPermissionChangeEvent(Member member, CommandPermission oldPermission, CommandPermission newPermission) {
		this.member = member;
		this.oldPermission = oldPermission;
		this.newPermission = newPermission;
	}

	public Member getMember() {
		return this.member;
	}

	public CommandPermission getOldPermission() {
		return this.oldPermission;
	}

	public CommandPermission getNewPermission() {
		return this.newPermission;
	}
}
