package burlap.oomdp.singleagent.environment;

import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.state.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

import java.util.LinkedList;
import java.util.List;

/**
 * An {@link burlap.oomdp.singleagent.environment.Environment} that simulates interactions using the {@link burlap.oomdp.singleagent.Action#performAction(State, burlap.oomdp.singleagent.GroundedAction)}
 * method of the the {@link burlap.oomdp.core.Domain} provided to this Environment. The rewards and terminal states are similarly tracked using a
 * provided {@link burlap.oomdp.singleagent.RewardFunction} and {@link burlap.oomdp.core.TerminalFunction}. Initial states of the environment
 * are defined using a {@link burlap.oomdp.auxiliary.StateGenerator}. If no {@link burlap.oomdp.auxiliary.StateGenerator} is specified,
 * but an initial {@link State} is provided in a constructor, then the {@link burlap.oomdp.auxiliary.StateGenerator} is
 * set to a {@link burlap.oomdp.auxiliary.common.ConstantStateGenerator} so that upon {@link #resetEnvironment()} method calls,
 * the initial state is the same as the original input state.
 * <p>
 * All returned environment observations are fully observable returning a copy of the true internal {@link State} of
 * the environment. Copies of the state are returned to prevent tampering of the internal environment state.
 * <p>
 * By default, this {@link burlap.oomdp.singleagent.environment.Environment} will not allow states to change when the current
 * environment state is a terminal state (as specified by the input {@link burlap.oomdp.core.TerminalFunction}); instead, the
 * same current state will be returned with a reward of zero if someone attempts to interact with the environment through {@link #executeAction(burlap.oomdp.singleagent.GroundedAction)}.
 * In this case, the environment state will have to be manually changed with {@link #resetEnvironment()} or {@link #setCurStateTo(State)}
 * to a non-terminal state before actions will affect the state again. Alternatively, you can allow actions to affect the state from
 * terminal states with the {@link #setAllowActionFromTerminalStates(boolean)} method.
 * @author James MacGlashan.
 */
public class SimulatedEnvironment implements StateSettableEnvironment, TaskSettableEnvironment, EnvironmentServerInterface{

	/**
	 * The domain of this environment
	 */
	protected Domain domain;

	/**
	 * The reward function of this environment
	 */
	protected RewardFunction rf;

	/**
	 * The terminal function for this environment
	 */
	protected TerminalFunction tf;

	/**
	 * The state generator used to generate new states when the environment is reset with {@link #resetEnvironment()};
	 */
	protected StateGenerator stateGenerator;

	/**
	 * The current state of the environment
	 */
	protected State curState;

	/**
	 * The last reward generated from this environment.
	 */
	protected double lastReward = 0.;

	/**
	 * A flag indicating whether the environment will respond to actions from a terminal state. If false,
	 * then once a the environment transitions to a terminal state, any action attempted by the {@link #executeAction(burlap.oomdp.singleagent.GroundedAction)}
	 * method will result in no change in state and to enable action again, the Environment state will have to be
	 * manually changed with the {@link #resetEnvironment()} method or the {@link #setCurStateTo(State)} method.
	 * If this value is true, then actions will be carried out according to the domain's transition dynamics.
	 */
	protected boolean allowActionFromTerminalStates = false;


	/**
	 * The {@link burlap.oomdp.singleagent.environment.EnvironmentObserver} objects that will be notified of {@link burlap.oomdp.singleagent.environment.Environment}
	 * events.
	 */
	protected List<EnvironmentObserver> observers = new LinkedList<EnvironmentObserver>();



	public SimulatedEnvironment(Domain domain, RewardFunction rf, TerminalFunction tf){
		this.domain = domain;
		this.rf = rf;
		this.tf = tf;
	}

	public SimulatedEnvironment(Domain domain, RewardFunction rf, TerminalFunction tf, State initialState) {
		this.domain = domain;
		this.rf = rf;
		this.tf = tf;
		this.stateGenerator = new ConstantStateGenerator(initialState);
		this.curState = initialState;
	}

	public SimulatedEnvironment(Domain domain, RewardFunction rf, TerminalFunction tf, StateGenerator stateGenerator) {
		this.domain = domain;
		this.rf = rf;
		this.tf = tf;
		this.stateGenerator = stateGenerator;
		this.curState = stateGenerator.generateState();
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public RewardFunction getRf() {
		return rf;
	}

	public void setRf(RewardFunction rf) {
		this.rf = rf;
	}

	public TerminalFunction getTf() {
		return tf;
	}

	public void setTf(TerminalFunction tf) {
		this.tf = tf;
	}

	public StateGenerator getStateGenerator() {
		return stateGenerator;
	}

	public void setStateGenerator(StateGenerator stateGenerator) {
		this.stateGenerator = stateGenerator;
	}

	@Override
	public void addObservers(EnvironmentObserver... observers) {
		for(EnvironmentObserver o : observers){
			this.observers.add(o);
		}
	}

	@Override
	public void clearAllObservers() {
		this.observers.clear();
	}

	@Override
	public void removeObservers(EnvironmentObserver... observers) {
		for(EnvironmentObserver o : observers){
			this.observers.remove(o);
		}
	}

	@Override
	public List<EnvironmentObserver> getObservers() {
		return this.observers;
	}

	/**
	 * Sets whether the environment will respond to actions from a terminal state. If false,
	 * then once a the environment transitions to a terminal state, any action attempted by the {@link #executeAction(burlap.oomdp.singleagent.GroundedAction)}
	 * method will result in no change in state and to enable action again, the Environment state will have to be
	 * manually changed with the {@link #resetEnvironment()} method or the {@link #setCurStateTo(State)} method.
	 * If this value is true, then actions will be carried out according to the domain's transition dynamics.
	 * @param allowActionFromTerminalStates if false, then actions are not allowed from terminal states; if true, then they are allowed.
	 */
	public void setAllowActionFromTerminalStates(boolean allowActionFromTerminalStates){
		this.allowActionFromTerminalStates = true;
	}

	@Override
	public void setCurStateTo(State s) {
		if(this.stateGenerator == null){
			this.stateGenerator = new ConstantStateGenerator(s);
		}
		this.curState = s;
	}

	@Override
	public State getCurrentObservation() {
		return this.curState.copy();
	}

	@Override
	public EnvironmentOutcome executeAction(GroundedAction ga) {

		GroundedAction simGA = (GroundedAction)ga.copy();
		simGA.action = this.domain.getAction(ga.actionName());
		if(simGA.action == null){
			throw new RuntimeException("Cannot execute action " + ga.toString() + " in this SimulatedEnvironment because the action is to known in this Environment's domain");
		}

		for(EnvironmentObserver observer : this.observers){
			observer.observeEnvironmentActionInitiation(this.getCurrentObservation(), ga);
		}

		State nextState;
		if(this.allowActionFromTerminalStates || !this.isInTerminalState()) {
			nextState = simGA.executeIn(this.curState);
			this.lastReward = this.rf.reward(this.curState, simGA, nextState);
		}
		else{
			nextState = this.curState;
			this.lastReward = 0.;
		}

		EnvironmentOutcome eo = new EnvironmentOutcome(this.curState.copy(), simGA, nextState.copy(), this.lastReward, this.tf.isTerminal(nextState));

		this.curState = nextState;

		for(EnvironmentObserver observer : this.observers){
			observer.observeEnvironmentInteraction(eo);
		}

		return eo;
	}

	@Override
	public double getLastReward() {
		return this.lastReward;
	}

	@Override
	public boolean isInTerminalState() {
		return this.tf.isTerminal(this.curState);
	}

	@Override
	public void resetEnvironment() {
		this.lastReward = 0.;
		this.curState = stateGenerator.generateState();
		for(EnvironmentObserver observer : this.observers){
			observer.observeEnvironmentReset(this);
		}
	}
}
