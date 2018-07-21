package io.axoniq.labs.chat.commandmodel;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

import java.util.HashSet;
import java.util.Set;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import io.axoniq.labs.chat.coreapi.CreateRoomCommand;
import io.axoniq.labs.chat.coreapi.JoinRoomCommand;
import io.axoniq.labs.chat.coreapi.LeaveRoomCommand;
import io.axoniq.labs.chat.coreapi.MessagePostedEvent;
import io.axoniq.labs.chat.coreapi.ParticipantJoinedRoomEvent;
import io.axoniq.labs.chat.coreapi.ParticipantLeftRoomEvent;
import io.axoniq.labs.chat.coreapi.PostMessageCommand;
import io.axoniq.labs.chat.coreapi.RoomCreatedEvent;

@Aggregate
public class ChatRoom {

	@AggregateIdentifier
	private String roomId;

	private Set<String> participants = new HashSet<>();

	@CommandHandler
	public ChatRoom(CreateRoomCommand cmd) {
		apply( new RoomCreatedEvent( cmd.getRoomId(), cmd.getName() ) );
	}

	@CommandHandler
	public void handle(JoinRoomCommand cmd) {
		if (!participants.contains( cmd.getParticipant() ))
			apply( new ParticipantJoinedRoomEvent( cmd.getParticipant(), cmd.getRoomId() ) );
	}

	@CommandHandler
	public void handle(LeaveRoomCommand cmd) {
		if (participants.contains( cmd.getParticipant() ))
			apply( new ParticipantLeftRoomEvent( cmd.getParticipant(), cmd.getRoomId() ) );
	}

	@CommandHandler
	public void handle(PostMessageCommand cmd) {
		if (!participants.contains( cmd.getParticipant() ))
			throw new IllegalStateException("You must join a room before posting!");

		apply( new MessagePostedEvent( cmd.getParticipant(), cmd.getRoomId(), cmd.getMessage() ) );
	}

	public ChatRoom() {
	}

	@EventSourcingHandler
	public void on(RoomCreatedEvent event) {
		roomId = event.getRoomId();
	}

	@EventSourcingHandler
	public void on(ParticipantJoinedRoomEvent event) {
		participants.add( event.getParticipant() );
	}

	@EventSourcingHandler
	public void on(ParticipantLeftRoomEvent event) {
		participants.remove( event.getParticipant() );
	}
}
