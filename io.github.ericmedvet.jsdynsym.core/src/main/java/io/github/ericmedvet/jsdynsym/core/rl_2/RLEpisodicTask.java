package io.github.ericmedvet.jsdynsym.core.rl_2;

import java.util.function.Consumer;

public interface RLEpisodicTask<S, O>{
    <P> O runEpisode(RLMethod<P> method, int timesteps, Consumer<S> listener);
}