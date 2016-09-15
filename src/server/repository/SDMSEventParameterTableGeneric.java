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
import java.util.*;
import java.lang.*;
import java.sql.*;

import de.independit.scheduler.server.*;
import de.independit.scheduler.server.util.*;
import de.independit.scheduler.server.exception.*;

public class SDMSEventParameterTableGeneric extends SDMSTable
{

	public final static String tableName = "EVENT_PARAMETER";
	public static SDMSEventParameterTable table  = null;

	public final static String[] columnNames = {
		"ID"
		, "KEY"
		, "VALUE"
		, "EVT_ID"
		, "CREATOR_U_ID"
		, "CREATE_TS"
		, "CHANGER_U_ID"
		, "CHANGE_TS"
	};
	public static SDMSIndex idx_evtId;
	public static SDMSIndex idx_eventId_key;

	public SDMSEventParameterTableGeneric(SystemEnvironment env)
	throws SDMSException
	{
		super(env);
		if (table != null) {
			throw new FatalException(new SDMSMessage(env, "01110182009", "EventParameter"));
		}
		table = (SDMSEventParameterTable) this;
		SDMSEventParameterTableGeneric.table = (SDMSEventParameterTable) this;
		isVersioned = false;
		idx_evtId = new SDMSIndex(env, SDMSIndex.ORDINARY, isVersioned, table, "evtId");
		idx_eventId_key = new SDMSIndex(env, SDMSIndex.UNIQUE, isVersioned, table, "eventId_key");
	}
	public SDMSEventParameter create(SystemEnvironment env
	                                 ,String p_key
	                                 ,String p_value
	                                 ,Long p_evtId
	                                )
	throws SDMSException
	{
		Long p_creatorUId = env.cEnv.uid();
		Long p_createTs = env.txTime();
		Long p_changerUId = env.cEnv.uid();
		Long p_changeTs = env.txTime();
		if(env.tx.mode == SDMSTransaction.READONLY) {
			throw new FatalException(new SDMSMessage(env, "01110182049", "EventParameter"));
		}
		validate(env
		         , p_key
		         , p_value
		         , p_evtId
		         , p_creatorUId
		         , p_createTs
		         , p_changerUId
		         , p_changeTs
		        );

		env.tx.beginSubTransaction(env);
		SDMSEventParameterGeneric o = new SDMSEventParameterGeneric(env
		                , p_key
		                , p_value
		                , p_evtId
		                , p_creatorUId
		                , p_createTs
		                , p_changerUId
		                , p_changeTs
		                                                           );

		SDMSEventParameter p;
		try {
			env.tx.addToTouchSet(env, o.versions, true);
			table.put(env, o.id, o.versions);
			env.tx.commitSubTransaction(env);
			p = (SDMSEventParameter)(o.toProxy(env));
			p.current = true;
		} catch(SDMSException e) {
			p = (SDMSEventParameter)(o.toProxy(env));
			p.current = true;
			env.tx.rollbackSubTransaction(env);
			throw e;
		}

		if(!checkCreatePrivs(env, p))
			throw new AccessViolationException(p.accessViolationMessage(env, "01402270738"));

		p.touchMaster(env);
		return p;
	}

	protected boolean checkCreatePrivs(SystemEnvironment env, SDMSEventParameter p)
	throws SDMSException
	{
		if(!p.checkPrivileges(env, SDMSPrivilege.CREATE))
			return false;

		return true;
	}

	protected void validate(SystemEnvironment env
	                        ,String p_key
	                        ,String p_value
	                        ,Long p_evtId
	                        ,Long p_creatorUId
	                        ,Long p_createTs
	                        ,Long p_changerUId
	                        ,Long p_changeTs
	                       )
	throws SDMSException
	{
	}

	protected SDMSObject rowToObject(SystemEnvironment env, ResultSet r)
	throws SDMSException
	{
		Long id;
		String key;
		String value;
		Long evtId;
		Long creatorUId;
		Long createTs;
		Long changerUId;
		Long changeTs;
		long validFrom;
		long validTo;

		try {
			id     = new Long (r.getLong(1));
			key = r.getString(2);
			value = r.getString(3);
			evtId = new Long (r.getLong(4));
			creatorUId = new Long (r.getLong(5));
			createTs = new Long (r.getLong(6));
			changerUId = new Long (r.getLong(7));
			changeTs = new Long (r.getLong(8));
			validFrom = 0;
			validTo = Long.MAX_VALUE;
		} catch(SQLException sqle) {
			SDMSThread.doTrace(null, "SQL Error : " + sqle.getMessage(), SDMSThread.SEVERITY_ERROR);
			throw new FatalException(new SDMSMessage(env, "01110182045", "EventParameter: $1 $2", new Integer(sqle.getErrorCode()), sqle.getMessage()));
		}
		if(validTo < env.lowestActiveVersion) return null;
		return new SDMSEventParameterGeneric(id,
		                                     key,
		                                     value,
		                                     evtId,
		                                     creatorUId,
		                                     createTs,
		                                     changerUId,
		                                     changeTs,
		                                     validFrom, validTo);
	}

	protected void loadTable(SystemEnvironment env)
	throws SQLException, SDMSException
	{
		int read = 0;
		int loaded = 0;

		final boolean postgres = SystemEnvironment.isPostgreSQL;
		String squote = SystemEnvironment.SQUOTE;
		String equote = SystemEnvironment.EQUOTE;
		Statement stmt = env.dbConnection.createStatement();
		ResultSet rset = stmt.executeQuery("SELECT " +
		                                   tableName() + ".ID" +
		                                   ", " + squote + "KEY" + equote +
		                                   ", " + squote + "VALUE" + equote +
		                                   ", " + squote + "EVT_ID" + equote +
		                                   ", " + squote + "CREATOR_U_ID" + equote +
		                                   ", " + squote + "CREATE_TS" + equote +
		                                   ", " + squote + "CHANGER_U_ID" + equote +
		                                   ", " + squote + "CHANGE_TS" + equote +
		                                   " FROM " + tableName() +
		                                   ""						  );
		while(rset.next()) {
			if(loadObject(env, rset)) ++loaded;
			++read;
		}
		stmt.close();
		SDMSThread.doTrace(null, "Read " + read + ", Loaded " + loaded + " rows for " + tableName(), SDMSThread.SEVERITY_INFO);
	}

	public String checkIndex(SDMSObject o)
	throws SDMSException
	{
		String out = "";
		boolean ok;
		ok =  idx_evtId.check(((SDMSEventParameterGeneric) o).evtId, o);
		out = out + "idx_evtId: " + (ok ? "ok" : "missing") + "\n";
		SDMSKey k;
		k = new SDMSKey();
		k.add(((SDMSEventParameterGeneric) o).evtId);
		k.add(((SDMSEventParameterGeneric) o).key);
		ok =  idx_eventId_key.check(k, o);
		out = out + "idx_eventId_key: " + (ok ? "ok" : "missing") + "\n";
		return out;
	}

	protected void index(SystemEnvironment env, SDMSObject o)
	throws SDMSException
	{
		index(env, o, -1);
	}

	protected void index(SystemEnvironment env, SDMSObject o, long indexMember)
	throws SDMSException
	{
		idx_evtId.put(env, ((SDMSEventParameterGeneric) o).evtId, o, ((1 & indexMember) != 0));
		SDMSKey k;
		k = new SDMSKey();
		k.add(((SDMSEventParameterGeneric) o).evtId);
		k.add(((SDMSEventParameterGeneric) o).key);
		idx_eventId_key.put(env, k, o, ((2 & indexMember) != 0));
	}

	protected  void unIndex(SystemEnvironment env, SDMSObject o)
	throws SDMSException
	{
		idx_evtId.remove(env, ((SDMSEventParameterGeneric) o).evtId, o);
		SDMSKey k;
		k = new SDMSKey();
		k.add(((SDMSEventParameterGeneric) o).evtId);
		k.add(((SDMSEventParameterGeneric) o).key);
		idx_eventId_key.remove(env, k, o);
	}

	public static SDMSEventParameter getObject(SystemEnvironment env, Long id)
	throws SDMSException
	{
		return (SDMSEventParameter) table.get(env, id);
	}

	public static SDMSEventParameter getObjectForUpdate(SystemEnvironment env, Long id)
	throws SDMSException
	{
		return (SDMSEventParameter) table.getForUpdate(env, id);
	}

	public static SDMSEventParameter getObject(SystemEnvironment env, Long id, long version)
	throws SDMSException
	{
		return (SDMSEventParameter) table.get(env, id, version);
	}

	public static SDMSEventParameter idx_eventId_key_getUnique(SystemEnvironment env, Object key)
	throws SDMSException
	{
		return (SDMSEventParameter)  SDMSEventParameterTableGeneric.idx_eventId_key.getUnique(env, key);
	}

	public static SDMSEventParameter idx_eventId_key_getUniqueForUpdate(SystemEnvironment env, Object key)
	throws SDMSException
	{
		return (SDMSEventParameter)  SDMSEventParameterTableGeneric.idx_eventId_key.getUniqueForUpdate(env, key);
	}

	public static SDMSEventParameter idx_eventId_key_getUnique(SystemEnvironment env, Object key, long version)
	throws SDMSException
	{
		return (SDMSEventParameter)  SDMSEventParameterTableGeneric.idx_eventId_key.getUnique(env, key, version);
	}

	public String tableName()
	{
		return tableName;
	}
	public String[] columnNames()
	{
		return columnNames;
	}
}
