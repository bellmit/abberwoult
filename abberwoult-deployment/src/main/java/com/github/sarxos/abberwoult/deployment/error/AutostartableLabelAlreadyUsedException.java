package com.github.sarxos.abberwoult.deployment.error;

import com.github.sarxos.abberwoult.annotation.Autostart;
import com.github.sarxos.abberwoult.deployment.item.InstrumentedActorBuildItem;


@SuppressWarnings("serial")
public class AutostartableLabelAlreadyUsedException extends IllegalArgumentException {

	public AutostartableLabelAlreadyUsedException(final InstrumentedActorBuildItem item, final InstrumentedActorBuildItem other, final String label) {
		super(""
			+ "Actor class " + item.getActorClassName() + " annotated with " + Autostart.class + " "
			+ "is labeled with '" + label + "', but actor class " + other.getActorClassName() + " "
			+ "also annotated with " + Autostart.class + " uses the very same label");
	}
}
