package game.utils;

import message.Message;

import java.net.InetAddress;

public record ClientPacket(InetAddress address, int port, Message message) {}
