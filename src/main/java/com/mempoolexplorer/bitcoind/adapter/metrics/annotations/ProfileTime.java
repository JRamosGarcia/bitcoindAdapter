/**
 * 
 */
package com.mempoolexplorer.bitcoind.adapter.metrics.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
/**
 * @author tomillo
 * 
 *         public methods marked with this annotation will be time-profiled
 *         using a {@Code GaugeService} and a
 *         {@Code com.google.common.base.Stopwatch}
 *
 */
public @interface ProfileTime {
	String metricName();// Metric name exposed in metrics actuator endpoint

	TimeUnit timeUnit() default TimeUnit.NANOSECONDS;

	boolean enableLog4J() default true;

}
