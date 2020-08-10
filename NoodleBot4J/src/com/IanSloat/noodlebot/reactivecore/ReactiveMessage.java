package com.IanSloat.noodlebot.reactivecore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.IanSloat.noodlebot.BotUtils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * A reactive message is a discord message that has event driven reactions used
 * as buttons. Similar to an ordinary message, a reactive message can be sent to
 * any text channel. All reactive messages use embedded content and cannot be
 * plaintext.
 */
public class ReactiveMessage extends ListenerAdapter {

	static final Logger logger = LoggerFactory.getLogger(ReactiveMessage.class);

	/**
	 * Used for general storage. List is specific to this reactive message
	 */
	public final List<Integer> integerTrackers = new ArrayList<Integer>();

	private final static Map<Message, ReactiveMessage> registeredMessages = new HashMap<>();
	private final List<ButtonListener> buttonListeners = new ArrayList<ButtonListener>();
	// private final List<Menu> menus = new ArrayList<Menu>(); // TODO Use with menu
	// class if it ever gets created
	// private int currentMenuIndex = 0; // TODO Use with menu class if it ever gets
	// created
	private Message registeredMessage;
	private TextChannel channel;
	private boolean isActive = false;
	private MessageEmbed messageBody;
	private Queue<Future<?>> priorityTasks = new LinkedList<>();
	private Future<?> monitorTask;
	private final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
	private final Lock monitorLock = new ReentrantLock();

	private synchronized void registerThisReactive() {
		if (isActive) {
			ReactiveMessage message = registeredMessages.get(this.getRegisteredMessage());

			if (message == null) {
				registeredMessages.put(this.getRegisteredMessage(), this);
				channel.getJDA().addEventListener(this);
			} else {
				logger.warn("A reactive tried to be registered but for some reason already exists");
			}
		}
	}

	private synchronized void unregisterThisReactive() {
		isActive = false;
		ReactiveMessage message = registeredMessages.get(this.getRegisteredMessage());
		if (message != null) {
			registeredMessages.remove(this.getRegisteredMessage(), this);
			channel.getJDA().removeEventListener(this);
		}
	}

	/**
	 * Creates a new empty reactive message. This message will not be sent upon
	 * creation. Instead, it will need to be constructed and activated using the
	 * activate() method.
	 * 
	 * @param channel The channel the message will be sent to upon activation
	 */
	public ReactiveMessage(TextChannel channel) {
		this.channel = channel;
	}

	/**
	 * Returns the guild this reactive message is or will be present in
	 * 
	 * @return the Guild object associated with this reactive message
	 */
	public Guild getGuild() {
		return channel.getGuild();
	}

	/**
	 * Returns the TextChannel this message is or will be present in
	 * 
	 * @return The TextChannel object associated with this reactive message
	 */
	public TextChannel getChannel() {
		return channel;
	}

	/**
	 * Returns the Message object associated with this reactive message. Usage of
	 * this method for manipulating the associated message is not recommended.
	 * Instead, use the setMessageContent() and update() methods
	 * 
	 * @return The Message object associated with this reactive message, null if the
	 *         reactive message has not been activated
	 */
	public Message getRegisteredMessage() {
		return this.registeredMessage;
	}

	/**
	 * Adds a button to the reactive message. If the associated emoji is already
	 * registered with a button, the previously existing button will be replaced
	 * 
	 * @param emoji  The emoji to register the button to. Will be a unicode string
	 *               if part of the official emoji typeset, or the name if a custom
	 *               emoji
	 * @param action The action to perform once clicked
	 */
	public void addButton(String emoji, Runnable action) {
		Button button = new Button(emoji, action);
		for (ButtonListener listener : buttonListeners) {
			if (listener.getButton().getEmojiName().equals(emoji)) {
				buttonListeners.remove(listener);
				break;
			}
		}
		buttonListeners.add(button);
	}

	/**
	 * Adds a button to the reactive message. If the associated emoji is already
	 * registered with a button, the previously existing button will be replaced
	 * 
	 * @param button The button to add or replace
	 */
	public void addButton(Button button) {
		int addIndex = 0;
		for (ButtonListener listener : buttonListeners) {
			if (listener.getButton().getEmojiName().equals(button.getEmojiName())) {
				addIndex = buttonListeners.indexOf(listener);
				buttonListeners.remove(listener);
				break;
			}
		}
		if (addIndex == 0) {
			buttonListeners.add(button);
		} else {
			buttonListeners.add(addIndex, button);
		}
	}

	/**
	 * Sets the embedded content that this reactive message will display in the
	 * associated TextChannel when activated
	 * 
	 * @param content The MessageEmbed object to set as the content of this reactive
	 *                message
	 */
	public void setMessageContent(MessageEmbed content) {
		this.messageBody = content;
	}

	/**
	 * If the content of this reactive message is not null and the message contains
	 * at least one button, this method will send the message to discord, adding
	 * necessary reactions and registering this reactive to the JDA event listener
	 * pool
	 */
	public void activate() {
		activate(null);
	}

	/**
	 * If the content of this reactive message is not null and the message contains
	 * at least one button, this method will send the message to discord, adding
	 * necessary reactions and registering this reactive to the JDA event listener
	 * pool
	 * 
	 * @param success The callback to execute when the reactive message activation
	 *                has completed
	 */
	public void activate(Consumer<Message> success) {
		if (messageBody != null && buttonListeners.size() > 0) {
			priorityTasks.add(ForkJoinPool.commonPool().submit(() -> {
				Message msg = channel.sendMessage(messageBody).complete();
				dispose();
				registeredMessage = msg;
				cleanMessage(msg.getReactions());
				isActive = true;
				registerThisReactive();
				if (success != null)
					success.accept(msg);
				monitorTask = ForkJoinPool.commonPool().submit(() -> {
					try {
						while (isActive) {
							while (priorityTasks.size() > 0) {
								try {
									priorityTasks.poll().get();
								} catch (ExecutionException e) {
									e.printStackTrace();
								}
							}
							relayEvent(eventQueue.take());
							monitorLock.lockInterruptibly();
						}
					} catch (InterruptedException e) {
						eventQueue.clear();
						if (monitorLock.tryLock())
							monitorLock.unlock();
					}
				});
			}));
		}
	}

	/**
	 * If the associated reactive message is active, this method will delete the
	 * associated message from discord and remove this reactive from the JDA event
	 * listener pool
	 */
	public void dispose() {
		unregisterThisReactive();
		if (registeredMessage != null)
			BotUtils.messageSafeDelete(registeredMessage);
		registeredMessage = null;
		isActive = false;
		if (monitorTask != null)
			monitorTask.cancel(true);
		eventQueue.clear();
		if (monitorLock.tryLock())
			monitorLock.unlock();
	}

	/**
	 * If the associated reactive message is active, this method will edit the
	 * message and set buttons to the current list of set buttons
	 */
	public void update() {
		if (isActive) {
			registeredMessage.editMessage(messageBody).queue((msg) -> cleanMessage(msg.getReactions()));
		}
	}

	private void alertButton(String emoji, User user) {
		buttonListeners.forEach((listener) -> listener.onButtonClick(new ButtonClickEvent(emoji, user)));
	}

	private void cleanMessage(List<MessageReaction> referenceList, User user) {
		try {
			List<String> knownEmojis = new ArrayList<String>();
			List<MessageReaction> processedList = new ArrayList<MessageReaction>();
			processedList.addAll(referenceList);
			buttonListeners.forEach((listener) -> knownEmojis.add(listener.getButton().getEmojiName()));
			referenceList.forEach((reaction) -> {
				String emoji = "";
				try {
					emoji = reaction.getReactionEmote().getAsCodepoints();
				} catch (IllegalStateException e) {
					emoji = reaction.getReactionEmote().getName();
				}
				if (!knownEmojis.contains(emoji)) {
					reaction.removeReaction(user).complete();
					processedList.remove(reaction);
				}
			});
			if (processedList.size() < knownEmojis.size()) {
				registeredMessage.clearReactions().complete();
				knownEmojis.forEach((emoji) -> registeredMessage.addReaction(emoji).complete());
			}
		} catch (Exception e) {
			if (registeredMessage != null)
				logger.warn("A clean attempt was made for reactive message (id:" + registeredMessage.getId()
						+ ") but the message either does not exist or a reaction failed to be added");
		}
	}

	private void cleanMessage(List<MessageReaction> referenceList) {
		try {
			List<String> knownEmojis = new ArrayList<String>();
			List<MessageReaction> processedList = new ArrayList<MessageReaction>();
			processedList.addAll(referenceList);
			buttonListeners.forEach((listener) -> knownEmojis.add(listener.getButton().getEmojiName()));
			if (processedList.size() < knownEmojis.size()) {
				registeredMessage.clearReactions().complete();
				knownEmojis.forEach((emoji) -> registeredMessage.addReaction(emoji).complete());
			}
		} catch (Exception e) {
			if (registeredMessage != null)
				logger.warn("A clean attempt was made for reactive message (id:" + registeredMessage.getId()
						+ ") but the message either does not exist or a reaction failed to be added");
		}
	}

	@Override
	public void onGenericMessageReaction(GenericMessageReactionEvent event) {
		if (event.getChannel().equals(registeredMessage.getChannel())
				&& event.getMessageId().equals(registeredMessage.getId()) && isActive && !event.getUser().isBot()) {
			eventQueue.add(event);
		}
	}

	@Override
	public void onMessageDelete(MessageDeleteEvent event) {
		if (event.getChannel().equals(registeredMessage.getChannel())
				&& event.getMessageId().equals(registeredMessage.getId())) {
			eventQueue.add(event);
		}
	}

	// Make sure all event actions are blocking here
	private void relayEvent(Event event) {
		ForkJoinPool.commonPool().submit(() -> {
			if (event instanceof GenericMessageReactionEvent) {
				Message msg = ((GenericMessageReactionEvent) event).getChannel()
						.retrieveMessageById(registeredMessage.getId()).complete();
				cleanMessage(msg.getReactions(), ((GenericMessageReactionEvent) event).getUser());
				if (!((GenericMessageReactionEvent) event).getUser().isBot()) {
					String emoji = "";
					try {
						emoji = ((GenericMessageReactionEvent) event).getReaction().getReactionEmote()
								.getAsCodepoints();
					} catch (IllegalStateException e) {
						emoji = ((GenericMessageReactionEvent) event).getReaction().getReactionEmote().getName();
					}
					alertButton(emoji, ((GenericMessageReactionEvent) event).getUser());
				}
			} else if (event instanceof MessageDeleteEvent) {
				unregisterThisReactive();
			}
			monitorLock.unlock();
		});
	}

}
