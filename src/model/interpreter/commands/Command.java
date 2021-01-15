package model.interpreter.commands;

import java.util.List;

public interface Command {
public int getArguments(String[] tokens,int idx,List <Object> emptyList);	
public void doCommand(List<Object> args);
}
