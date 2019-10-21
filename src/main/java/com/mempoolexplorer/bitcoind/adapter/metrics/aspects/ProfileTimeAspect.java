package com.mempoolexplorer.bitcoind.adapter.metrics.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Stopwatch;
import com.mempoolexplorer.bitcoind.adapter.metrics.annotations.ProfileTime;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Aspect
@Component
public class ProfileTimeAspect {

	@Autowired
	private MeterRegistry registry;

	@Around("execution(* *(..)) and @annotation(profileTime)")
	public Object profile(ProceedingJoinPoint pjp, ProfileTime profileTime) throws Throwable {

		Stopwatch stopwatch = Stopwatch.createStarted();
		Object ret = pjp.proceed();
		stopwatch.stop();
		if (profileTime.enableLog4J()) {
			Logger logger = LoggerFactory.getLogger(ProfileTimeAspect.class);
			logger.debug("Profile of " + profileTime.metricName() + ": " + stopwatch.toString());
		}
		Timer timer = registry.timer(profileTime.metricName());
		timer.record(stopwatch.elapsed());		
		return ret;
	}
}
