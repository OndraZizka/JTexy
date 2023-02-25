package cz.dynawest.jtexy.modules;

/**
 * Interface for inter-module callbacks in LinkProcessListener.
 */
public interface LinkProvider {

	public TexyLink getLink(String key);
}
