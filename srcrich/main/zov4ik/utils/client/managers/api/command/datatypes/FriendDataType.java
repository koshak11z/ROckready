package im.zov4ik.utils.client.managers.api.command.datatypes;

import im.zov4ik.utils.client.managers.api.command.exception.CommandException;
import im.zov4ik.utils.client.managers.api.command.helpers.TabCompleteHelper;
import im.zov4ik.common.repository.friend.Friend;
import im.zov4ik.common.repository.friend.FriendUtils;

import java.util.List;
import java.util.stream.Stream;

public enum FriendDataType implements IDatatypeFor<Friend> {
    INSTANCE;

    @Override
    public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
        Stream<String> friends = getFriends()
                .stream()
                .map(Friend::getName);

        String context = datatypeContext
                .getConsumer()
                .getString();

        return new TabCompleteHelper()
                .append(friends)
                .filterPrefix(context)
                .sortAlphabetically()
                .stream();
    }

    @Override
    public Friend get(IDatatypeContext datatypeContext) throws CommandException {
        String username = datatypeContext
                .getConsumer()
                .getString();

        return getFriends().stream()
                .filter(s -> s.getName().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    private List<? extends Friend> getFriends() {
        return FriendUtils.getFriends();
    }
}
