/*
Copyright (c) 2000-2013 "independIT Integrative Technologies GmbH",
Authors: Ronald Jeninga, Dieter Stubler

BICsuite!Open Enterprise Job Scheduling System

independIT Integrative Technologies GmbH [http://www.independit.de]
mailto:contact@independit.de

This file is part of BICsuite!Open

BICsuite!Open is free software:
you can redistribute it and/or modify it under the terms of the
GNU Affero General Public License as published by the
Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/


package de.independit.scheduler.server.repository;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.sql.*;

import de.independit.scheduler.server.*;
import de.independit.scheduler.server.util.*;
import de.independit.scheduler.server.exception.*;

public class SDMSSubmittedEntityTable extends SDMSSubmittedEntityTableGeneric
{

	public final static String __version = "@(#) $Id: SDMSSubmittedEntityTable.java,v 2.22.2.2 2013/03/22 14:48:03 ronald Exp $";
	static Long lzero = new Long(0);
	static Integer zero = new Integer(0);
	static Float fzero = new Float(0);

	public SDMSSubmittedEntityTable(SystemEnvironment env)
	throws SDMSException
	{
		super(env);
	}

	public SDMSSubmittedEntity create(SystemEnvironment env
	                                  ,Long p_accessKey
	                                  ,Long p_masterId
	                                  ,String p_submitTag
	                                  ,Integer p_unresolvedHandling
	                                  ,Long p_seId
	                                  ,String p_childTag
	                                  ,Long p_seVersion
	                                  ,Long p_ownerId
	                                  ,Long p_parentId
	                                  ,Long p_scopeId
	                                  ,Boolean p_isStatic
	                                  ,Integer p_mergeMode
	                                  ,Integer p_state
	                                  ,Long p_jobEsdId
	                                  ,Integer p_jobEsdPref
	                                  ,Boolean p_jobIsFinal
	                                  ,Boolean p_jobIsRestartable
	                                  ,Long p_finalEsdId
	                                  ,Integer p_exitCode
	                                  ,String p_commandline
	                                  ,String p_rrCommandline
	                                  ,Integer p_rerunSeq
	                                  ,Boolean p_isReplaced
	                                  ,Boolean p_isCancelled
	                                  ,Long p_originSmeId
	                                  ,Long p_reasonSmeId
	                                  ,Long p_fireSmeId
	                                  ,Long p_fireSeId
	                                  ,Long p_trId
	                                  ,Long p_trSdIdOld
	                                  ,Long p_trSdIdNew
	                                  ,Integer p_trSeq
	                                  ,String p_workdir
	                                  ,String p_logfile
	                                  ,String p_errlogfile
	                                  ,String p_pid
	                                  ,String p_extPid
	                                  ,String p_errorMsg
	                                  ,Long p_killId
	                                  ,Integer p_killExitCode
	                                  ,Boolean p_isSuspended
	                                  ,Integer p_priority
	                                  ,Integer p_nice
	                                  ,Integer p_minEP
	                                  ,Integer p_agingAmount
	                                  ,Integer p_parentSuspended
	                                  ,Integer p_childSuspended
	                                  ,Integer p_warnCount
	                                  ,Long p_warnLink
	                                  ,Long p_submitTs
	                                  ,Long p_resumeTs
	                                  ,Long p_syncTs
	                                  ,Long p_resourceTs
	                                  ,Long p_runnableTs
	                                  ,Long p_startTs
	                                  ,Long p_finishTs
	                                  ,Long p_finalTs
	                                  ,Integer p_cntSubmitted
	                                  ,Integer p_cntDependencyWait
	                                  ,Integer p_cntSynchronizeWait
	                                  ,Integer p_cntResourceWait
	                                  ,Integer p_cntRunnable
	                                  ,Integer p_cntStarting
	                                  ,Integer p_cntStarted
	                                  ,Integer p_cntRunning
	                                  ,Integer p_cntToKill
	                                  ,Integer p_cntKilled
	                                  ,Integer p_cntCancelled
	                                  ,Integer p_cntFinished
	                                  ,Integer p_cntFinal
	                                  ,Integer p_cntBrokenActive
	                                  ,Integer p_cntBrokenFinished
	                                  ,Integer p_cntError
	                                  ,Integer p_cntUnreachable
	                                  ,Integer p_cntRestartable
	                                  ,Integer p_cntWarn
	                                  ,Integer p_cntPending
	                                 )
	throws SDMSException
	{
		SDMSSubmittedEntity sme = super.create(env
		                                       ,p_accessKey
		                                       ,p_masterId
		                                       ,p_submitTag
		                                       ,p_unresolvedHandling
		                                       ,p_seId
		                                       ,p_childTag
		                                       ,p_seVersion
		                                       ,p_ownerId
		                                       ,p_parentId
		                                       ,p_scopeId
		                                       ,p_isStatic
		                                       ,p_mergeMode
		                                       ,p_state
		                                       ,p_jobEsdId
		                                       ,p_jobEsdPref
		                                       ,p_jobIsFinal
		                                       ,p_jobIsRestartable
		                                       ,p_finalEsdId
		                                       ,p_exitCode
		                                       ,p_commandline
		                                       ,p_rrCommandline
		                                       ,p_rerunSeq
		                                       ,p_isReplaced
		                                       ,p_isCancelled
		                                       ,p_originSmeId
		                                       ,p_reasonSmeId
		                                       ,p_fireSmeId
		                                       ,p_fireSeId
		                                       ,p_trId
		                                       ,p_trSdIdOld
		                                       ,p_trSdIdNew
		                                       ,p_trSeq
		                                       ,p_workdir
		                                       ,p_logfile
		                                       ,p_errlogfile
		                                       ,p_pid
		                                       ,p_extPid
		                                       ,p_errorMsg
		                                       ,p_killId
		                                       ,p_killExitCode
		                                       ,p_isSuspended
		                                       ,Boolean.FALSE
		                                       ,p_priority
		                                       ,p_nice
		                                       ,p_minEP
		                                       ,p_agingAmount
		                                       ,p_parentSuspended
		                                       ,p_childSuspended
		                                       ,p_warnCount
		                                       ,p_warnLink
		                                       ,p_submitTs
		                                       ,p_resumeTs
		                                       ,p_syncTs
		                                       ,p_resourceTs
		                                       ,p_runnableTs
		                                       ,p_startTs
		                                       ,p_finishTs
		                                       ,p_finalTs
		                                       ,p_cntSubmitted
		                                       ,p_cntDependencyWait
		                                       ,p_cntSynchronizeWait
		                                       ,p_cntResourceWait
		                                       ,p_cntRunnable
		                                       ,p_cntStarting
		                                       ,p_cntStarted
		                                       ,p_cntRunning
		                                       ,p_cntToKill
		                                       ,p_cntKilled
		                                       ,p_cntCancelled
		                                       ,p_cntFinished
		                                       ,p_cntFinal
		                                       ,p_cntBrokenActive
		                                       ,p_cntBrokenFinished
		                                       ,p_cntError
		                                       ,p_cntUnreachable
		                                       ,p_cntRestartable
		                                       ,p_cntWarn
		                                       ,p_cntPending
		                                      );

		final Long smeId = sme.getId(env);

		long seVersion = p_seVersion.longValue();
		if (p_masterId.longValue() == 0) {
			env.seVersionList.add(env, seVersion);
			sme.setMasterId(env, smeId);
			p_masterId = smeId;
		}

		int cnt = env.tx.smeCtr.intValue() + 1;
		env.tx.smeCtr = new Integer(cnt);

		return sme;
	}

	public SDMSSubmittedEntity createErrorMaster(SystemEnvironment env
	                ,Long p_seId
	                ,Long p_seVersion
	                ,Long p_ownerId
	                ,Long p_jobEsdId
	                ,Long p_finalEsdId
	                ,String p_errorMsg
	                ,Integer p_priority
	                ,Integer p_nice
	                ,Long p_submitTs
	                                            )
	throws SDMSException
	{
		SDMSSubmittedEntity sme = super.create(env
		                                       ,env.randomLong()
		                                       ,new Long(0)
		                                       ,null
		                                       ,new Integer(SDMSDependencyDefinition.ERROR)
		                                       ,p_seId
		                                       ,null
		                                       ,p_seVersion
		                                       ,p_ownerId
		                                       ,null
		                                       ,null
		                                       ,Boolean.TRUE
		                                       ,new Integer(SDMSSchedulingHierarchy.FAILURE)
		                                       ,new Integer(SDMSSubmittedEntity.ERROR)
		                                       ,p_jobEsdId
		                                       ,zero
		                                       ,Boolean.FALSE
		                                       ,Boolean.FALSE
		                                       ,p_finalEsdId
		                                       ,null
		                                       ,null
		                                       ,null
		                                       ,zero
		                                       ,Boolean.FALSE
		                                       ,Boolean.FALSE
		                                       ,null
		                                       ,null
		                                       ,null
		                                       ,null
		                                       ,null
		                                       ,null
		                                       ,null
		                                       ,zero
		                                       ,null
		                                       ,null
		                                       ,null
		                                       ,null
		                                       ,null
		                                       ,p_errorMsg
		                                       ,null
		                                       ,null
		                                       ,Boolean.FALSE
		                                       ,Boolean.FALSE
		                                       ,p_priority
		                                       ,p_nice
		                                       ,zero
		                                       ,zero
		                                       ,zero
		                                       ,zero
		                                       ,zero
		                                       ,null
		                                       ,p_submitTs
		                                       ,null
		                                       ,null
		                                       ,null
		                                       ,null
		                                       ,null
		                                       ,null
		                                       ,null
		                                       , zero, zero, zero, zero,
		                                       zero, zero, zero, zero, zero,
		                                       zero, zero, zero, zero, zero,
		                                       zero, zero, zero, zero, zero,
		                                       zero
		                                      );

		final Long smeId = sme.getId(env);
		long seVersion = p_seVersion.longValue();
		env.seVersionList.add(env, seVersion);
		sme.setMasterId(env, smeId);

		int cnt = env.tx.smeCtr.intValue() + 1;
		env.tx.smeCtr = new Integer(cnt);

		return sme;
	}

	protected SDMSObject rowToObject(SystemEnvironment env, ResultSet r)
	throws SDMSException
	{
		SDMSSubmittedEntityGeneric smeg = (SDMSSubmittedEntityGeneric)(super.rowToObject(env, r));

		if (smeg.getId(env).longValue() == smeg.getMasterId(env).longValue()) {
			env.seVersionList.add(env, smeg.getSeVersion(env).longValue());
		}

		return smeg;
	}
}