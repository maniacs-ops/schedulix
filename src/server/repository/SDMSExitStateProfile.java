/*
Copyright (c) 2000-2013 "independIT Integrative Technologies GmbH",
Authors: Ronald Jeninga, Dieter Stubler

schedulix Enterprise Job Scheduling System

independIT Integrative Technologies GmbH [http://www.independit.de]
mailto:contact@independit.de

This file is part of schedulix

schedulix is free software:
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
import java.lang.*;
import java.util.*;
import java.sql.*;

import de.independit.scheduler.server.*;
import de.independit.scheduler.server.util.*;
import de.independit.scheduler.server.exception.*;

public class SDMSExitStateProfile extends SDMSExitStateProfileProxyGeneric
{

	public final static String __version = "@(#) $Id: SDMSExitStateProfile.java,v 2.7.2.2 2013/03/19 17:16:51 ronald Exp $";

	protected SDMSExitStateProfile(SDMSObject p_object)
	{
		super(p_object);
	}

	public void validateMappingProfile(SystemEnvironment env, Long esmpId)
		throws SDMSException
	{
		Long espId = getId(env);
		Vector esm_v = SDMSExitStateMappingTable.idx_esmpId.getVector(env, esmpId);
		Vector es_v = SDMSExitStateTable.idx_espId.getVector(env, espId);
		Iterator i;
		Iterator j;
		i = esm_v.iterator();
		boolean found;
		Long esm_esdId;
		Long es_esdId;
		while (i.hasNext()) {
			esm_esdId = ((SDMSExitStateMapping)(i.next())).getEsdId(env);
			found = false;
			j = es_v.iterator();
			while (j.hasNext()) {
				es_esdId = ((SDMSExitState)(j.next())).getEsdId(env);
				if (esm_esdId.equals(es_esdId)) {
					found = true;
					break;
				}
			}
			if (found == false) {
				throw new CommonErrorException (new SDMSMessage (env, "02111081735",
					"Exit State Mapping not compatible with Exit State Profile"));
			}
		}
	}

	public void checkProfile(SystemEnvironment sysEnv)
		throws SDMSException
	{
		Long espId = getId(sysEnv);

		Long defaultEsmpId = getDefaultEsmpId(sysEnv);
		if (defaultEsmpId != null) {
			validateMappingProfile(sysEnv, defaultEsmpId);
		}

		Vector se_v = SDMSSchedulingEntityTable.idx_espId.getVector(sysEnv, espId);
		SDMSSchedulingEntity se;
		Long se_esmpId;
		Iterator i = se_v.iterator();
		while (i.hasNext()) {
			se = (SDMSSchedulingEntity)i.next();

			se_esmpId = se.getEsmpId(sysEnv);
			if (se_esmpId != null) {
				try {
					validateMappingProfile(sysEnv, se_esmpId);
				} catch (CommonErrorException ce) {
					SDMSExitStateMappingProfile se_esmp;
					se_esmp = SDMSExitStateMappingProfileTable.getObject(sysEnv, se_esmpId);
					throw new CommonErrorException (new SDMSMessage(sysEnv, "02112191851",
						"Exit State Mapping $1 of Job $2 not copatible with Profile",
						se_esmp.getName(sysEnv), se.getName(sysEnv)));
				}
			}

			Long timeoutStateId = se.getTimeoutStateId(sysEnv);
			if(timeoutStateId != null && !SDMSExitStateTable.idx_espId_esdId.containsKey(sysEnv, new SDMSKey(espId, timeoutStateId))) {
				throw new CommonErrorException(new SDMSMessage(sysEnv, "03311051011",
					"Timeout state $1 of $2 not defined within profile",
					SDMSExitStateDefinitionTable.getObject(sysEnv, timeoutStateId).getName(sysEnv),
					se.pathString(sysEnv)));
			}

			checkChildTranslations(sysEnv, espId, se);

			checkParentTranslations(sysEnv, espId, se);

			checkDependencies(sysEnv, espId, se);
		}
		setIsValid(sysEnv, Boolean.TRUE);
	}

	private void checkChildTranslations(
	        SystemEnvironment sysEnv,
	        Long espId,
	        SDMSSchedulingEntity se
	)
	throws SDMSException
	{
		Vector sec_v = SDMSSchedulingHierarchyTable.idx_seParentId.getVector(sysEnv, se.getId(sysEnv));

		SDMSSchedulingHierarchy sh;
		SDMSSchedulingEntity seChild;
		Long espIdChild;
		SDMSExitStateTranslationProfile estp;

		Long estpId;
		Iterator ic = sec_v.iterator();
		while (ic.hasNext()) {
			sh = (SDMSSchedulingHierarchy)ic.next();
			seChild = SDMSSchedulingEntityTable.getObject(sysEnv, sh.getSeChildId(sysEnv));
			estpId = sh.getEstpId(sysEnv);
			if(estpId == null) {
				estp = null;
			} else {
				estp = SDMSExitStateTranslationProfileTable.getObject(sysEnv, estpId);
			}
			espIdChild = seChild.getEspId(sysEnv);

			if(estp == null) {
				continue;
			}

			Vector v_childEs = SDMSExitStateTable.idx_espId.getVector(sysEnv, espIdChild);
			Long esdIdChild;
			Long esdIdParent;
			SDMSKey k;
			Iterator ices = v_childEs.iterator();
			while (ices.hasNext()) {
				SDMSExitState ces = (SDMSExitState)ices.next();
				esdIdChild = ces.getEsdId(sysEnv);
				esdIdParent = esdIdChild;

				if(estp != null) {
					esdIdParent = estp.translate(sysEnv, esdIdChild, false);
					if (esdIdParent == null) continue;
				}

				k = new SDMSKey (espId, esdIdParent);
				if (!SDMSExitStateTable.idx_espId_esdId.containsKey(sysEnv, k)) {
					Object[] p = new Object[5];
					p[0] = SDMSExitStateDefinitionTable.getObject(sysEnv, esdIdChild).getName(sysEnv);
					p[1] = seChild.pathString(sysEnv);
					p[2] = SDMSExitStateDefinitionTable.getObject(sysEnv, esdIdParent).getName(sysEnv);
					p[3] = se.pathString(sysEnv);
					p[4] = ( estp == null ?  "NONE" : estp.getName(sysEnv));
					throw new CommonErrorException(new SDMSMessage (sysEnv, "02112172235",
					                               "Profile doesn't contain state $3 of $4 translated from child state $1 of $2, Translation = [$5]", p));
				} else {
					SDMSExitState pes = SDMSExitStateTable.idx_espId_esdId_getUnique(sysEnv, k);
					if (pes.getIsFinal(sysEnv).equals(Boolean.FALSE)) {
						if (ces.getIsFinal(sysEnv).equals(Boolean.TRUE)) {
							Object[] p = new Object[5];
							p[0] = SDMSExitStateDefinitionTable.getObject(sysEnv, esdIdChild).getName(sysEnv);
							p[1] = seChild.pathString(sysEnv);
							p[2] = SDMSExitStateDefinitionTable.getObject(sysEnv, esdIdParent).getName(sysEnv);
							p[3] = se.pathString(sysEnv);
							p[4] = ( estp == null ?  "NONE" : estp.getName(sysEnv));
							throw new CommonErrorException(new SDMSMessage (sysEnv, "02205061627",
							                               "Invalid trasnlation from final child state $1 of $2 to non final state $3 of $4, Translation = [$5]", p));
						}
					}
				}
			}
		}
	}

	private void checkParentTranslations(
	        SystemEnvironment sysEnv,
	        Long espId,
	        SDMSSchedulingEntity se
	)
	throws SDMSException
	{
		Vector v_es = SDMSExitStateTable.idx_espId.getVector(sysEnv, espId);
		Vector v_sep = SDMSSchedulingHierarchyTable.idx_seChildId.getVector(sysEnv, se.getId(sysEnv));
		SDMSSchedulingHierarchy sh;
		SDMSSchedulingEntity seParent;
		Long espIdParent;
		Long estpId;
		SDMSExitStateTranslationProfile estp;
		Iterator ip = v_sep.iterator();
		while (ip.hasNext()) {
			sh = (SDMSSchedulingHierarchy)ip.next();
			seParent = SDMSSchedulingEntityTable.getObject(sysEnv, sh.getSeParentId(sysEnv));
			estpId = sh.getEstpId(sysEnv);

			if(estpId == null) {
				continue;
			} else {
				estp = SDMSExitStateTranslationProfileTable.getObject(sysEnv, estpId);
			}

			espIdParent = seParent.getEspId(sysEnv);
			if (espIdParent.equals(espId)) {
				continue;
			}
			Long esdIdChild;
			Long esdIdParent;
			SDMSKey k;
			Iterator ipes = v_es.iterator();
			while (ipes.hasNext()) {
				SDMSExitState ces = (SDMSExitState)ipes.next();
				esdIdChild = ces.getEsdId(sysEnv);
				esdIdParent = esdIdChild;

				esdIdParent = estp.translate(sysEnv, esdIdChild, false);
				if (esdIdParent == null) continue;
				k = new SDMSKey (espIdParent, esdIdParent);
				if (!SDMSExitStateTable.idx_espId_esdId.containsKey(sysEnv, k)) {
					SDMSSchedulingEntity seChild = SDMSSchedulingEntityTable.getObject(sysEnv, sh.getSeChildId(sysEnv));
					Object[] p = new Object[4];
					p[0] = SDMSExitStateDefinitionTable.getObject(sysEnv, esdIdChild).getName(sysEnv);
					p[1] = seChild.pathString(sysEnv);
					p[2] = seParent.pathString(sysEnv);
					p[3] = estp.getName(sysEnv);
					throw new CommonErrorException(new SDMSMessage (sysEnv, "03201292033",
					                               "Profile contains state $1 not translating from $2 to $3 Translation $4", p));
				} else {
					SDMSExitState pes = SDMSExitStateTable.idx_espId_esdId_getUnique(sysEnv, k);
					if (pes.getIsFinal(sysEnv).equals(Boolean.FALSE)) {
						if (ces.getIsFinal(sysEnv).equals(Boolean.TRUE)) {
							Object[] p = new Object[5];
							p[0] = SDMSExitStateDefinitionTable.getObject(sysEnv, esdIdChild).getName(sysEnv);
							p[1] = se.pathString(sysEnv);
							p[2] = SDMSExitStateDefinitionTable.getObject(sysEnv, esdIdParent).getName(sysEnv);
							p[3] = seParent.pathString(sysEnv);
							p[4] = (estp == null ? "NONE" : estp.getName(sysEnv));
							throw new CommonErrorException(new SDMSMessage (sysEnv, "02205061726",
							                               "Invalid translation from final child state $1 of $2 to non final state $3 of $4, Translation = [$5]", p));
						}
					}
				}
			}
		}
	}

	private void checkDependencies(
			SystemEnvironment sysEnv,
			Long espId,
			SDMSSchedulingEntity se
		)
		throws SDMSException
	{
		Vector v_es = SDMSExitStateTable.idx_espId.getVector(sysEnv, espId);
		Vector v_dd = SDMSDependencyDefinitionTable.idx_seRequiredId.getVector(sysEnv, se.getId(sysEnv));
		SDMSDependencyDefinition dd;
		Iterator idd = v_dd.iterator();
		while (idd.hasNext()) {
			dd = (SDMSDependencyDefinition)idd.next();
			Vector v_ds = SDMSDependencyStateTable.idx_ddId.getVector(sysEnv, dd.getId(sysEnv));
			Iterator ids = v_ds.iterator();
			dependencyStateLoop:
			while (ids.hasNext()) {
				SDMSDependencyState ds = (SDMSDependencyState)ids.next();
				Long esdId = ds.getEsdId(sysEnv);
				Iterator ies = v_es.iterator();
				while (ies.hasNext()) {
					SDMSExitState es = (SDMSExitState)ies.next();
					if (esdId.equals(es.getEsdId(sysEnv)) && es.getIsFinal(sysEnv).booleanValue()) {
						continue dependencyStateLoop;
					}
				}
				SDMSSchedulingEntity se_dep = SDMSSchedulingEntityTable.getObject(sysEnv, dd.getSeDependentId(sysEnv));
				SDMSExitStateDefinition esd = SDMSExitStateDefinitionTable.getObject(sysEnv, esdId);
				throw new CommonErrorException(new SDMSMessage(sysEnv, "02112201408",
					"Job $1 depends on Exit State $2 of Job $3, not longer a final exit state",
					se_dep.pathString(sysEnv), esd.getName(sysEnv),se.pathString(sysEnv)
					));
			}
		}
	}

	public boolean isPendingState(SystemEnvironment sysEnv, Long esdId, long seVersion)
		throws SDMSException
	{
		if (esdId == null) return false;
		Long id = getId(sysEnv);
		SDMSExitState es;
		es = SDMSExitStateTable.idx_espId_esdId_getUnique(sysEnv, new SDMSKey (id, esdId), seVersion);
		return !es.getIsFinal(sysEnv).booleanValue() && !es.getIsRestartable(sysEnv).booleanValue();
	}

	public Long getBrokenState(SystemEnvironment sysEnv)
		throws SDMSException
	{
		if(sysEnv.tx.mode == SDMSTransaction.READWRITE) {
			return getBrokenState(sysEnv, Long.MAX_VALUE);
		} else {
			return getBrokenState(sysEnv, sysEnv.tx.versionId);
		}
	}
	public Long getBrokenState(SystemEnvironment sysEnv, long seVersion)
		throws SDMSException
	{
		SDMSExitState es;
		final Vector v;

		v = SDMSExitStateTable.idx_espId.getVector(sysEnv, getId(sysEnv), seVersion);
		for(int i = 0; i < v.size(); i++) {
			es = (SDMSExitState) v.get(i);
			if(es.getIsBroken(sysEnv).booleanValue())
				return es.getEsdId(sysEnv);
		}
		return null;
	}

	public Long getUnreachableState(SystemEnvironment sysEnv)
		throws SDMSException
	{
		if(sysEnv.tx.mode == SDMSTransaction.READWRITE) {
			return getUnreachableState(sysEnv, Long.MAX_VALUE);
		} else {
			return getUnreachableState(sysEnv, sysEnv.tx.versionId);
		}
	}
	public Long getUnreachableState(SystemEnvironment sysEnv, long seVersion)
		throws SDMSException
	{
		SDMSExitState es;
		final Vector v;

		v = SDMSExitStateTable.idx_espId.getVector(sysEnv, getId(sysEnv), seVersion);
		for(int i = 0; i < v.size(); i++) {
			es = (SDMSExitState) v.get(i);
			if(es.getIsUnreachable(sysEnv).booleanValue())
				return es.getEsdId(sysEnv);
		}
		return null;
	}

	public Integer getUnreachableStatePreference(SystemEnvironment sysEnv)
		throws SDMSException
	{
		if(sysEnv.tx.mode == SDMSTransaction.READWRITE) {
			return getUnreachableStatePreference(sysEnv, Long.MAX_VALUE);
		} else {
			return getUnreachableStatePreference(sysEnv, sysEnv.tx.versionId);
		}
	}
	public Integer getUnreachableStatePreference(SystemEnvironment sysEnv, long seVersion)
		throws SDMSException
	{
		SDMSExitState es;
		final Vector v;

		v = SDMSExitStateTable.idx_espId.getVector(sysEnv, getId(sysEnv), seVersion);
		for(int i = 0; i < v.size(); i++) {
			es = (SDMSExitState) v.get(i);
			if(es.getIsUnreachable(sysEnv).booleanValue())
				return es.getPreference(sysEnv);
		}
		return null;
	}

	public String getURLName(SystemEnvironment sysEnv)
		throws SDMSException
	{
		return getName(sysEnv);
	}

	public String getURL(SystemEnvironment sysEnv)
		throws SDMSException
	{
		return "exit state profile " + getURLName(sysEnv);
	}
}
