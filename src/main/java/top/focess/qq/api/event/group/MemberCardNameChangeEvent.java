package top.focess.qq.api.event.group;

import top.focess.qq.api.bot.contact.Member;
import top.focess.qq.api.event.Event;
import top.focess.qq.api.event.ListenerHandler;

/**
 * Called after member's card name changed
 */
public class MemberCardNameChangeEvent extends Event {

	private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

	/**
	 * The member
	 */
	private final Member member;

	/**
	 * The old card name
	 */
	private final String oldCardName;

	/**
	 * The new card name
	 */
	private final String newCardName;

	/**
	 * Constructs a MemberCardNameChangeEvent
	 * @param member the member
	 * @param oldCardName the old card name
	 * @param newCardName the new card name
	 */
	public MemberCardNameChangeEvent(Member member, String oldCardName, String newCardName) {
		this.member = member;
		this.oldCardName = oldCardName;
		this.newCardName = newCardName;
	}

	public Member getMember() {
		return member;
	}

	public String getOldCardName() {
		return oldCardName;
	}

	public String getNewCardName() {
		return newCardName;
	}
}
