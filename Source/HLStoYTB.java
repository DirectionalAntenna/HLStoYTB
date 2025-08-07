/*
* This code and all components (c) Copyright 2018, Wowza Media Systems, LLC. All rights reserved.
* This code is licensed pursuant to the BSD 3-Clause License.
* Add init method to handler classes to set playlistCrossName field correctly
*/
//////////////////////////////////////////////////////////////////////////////////////////////////////////
// About kr.eqmaker.wms.hls.push.youtube 1.29
// This code of the package(kr.eqmaker.wms.hls.push.youtube) Copyright 2025, EQMaker All rights reserved.
// This code is based on wse-example-pushpublish-hls of Wowza Media Systems, LLC.
// This code is licensed pursuant to the BSD 3-Clause License.
// EQMaker make no warranties regarding the performance or operation of this program.
// The user assumes all responsibility and risk for the use of this code, package and program. 
//////////////////////////////////////////////////////////////////////////////////////////////////////////
package kr.eqmaker.wms.hls.push.youtube;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.wowza.util.IPacketFragment;
import com.wowza.util.PacketFragmentList;
import com.wowza.util.StringUtils;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.manifest.model.m3u8.MediaSegmentModel;
import com.wowza.wms.manifest.model.m3u8.PlaylistModel;
import com.wowza.wms.manifest.writer.m3u8.PlaylistWriter;
import com.wowza.wms.pushpublish.manager.IPushPublisher;
import com.wowza.wms.pushpublish.protocol.cupertino.PushPublishHTTPCupertino;
import com.wowza.wms.server.LicensingException;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.util.PushPublishUtils;

public class HLStoYTB extends PushPublishHTTPCupertino
{
	private static final int DEFAULT_HTTP_PORT = 80;
	private static final int DEFAULT_HTTPS_PORT = 443;

	String basePath = "/";
	String httpHost = "eqmaker.kr";

	boolean isSendSSL = false;
	boolean LogDetail = false;

	boolean backup = false;
	String groupName = null;
	private int connectionTimeout = 5000;
	private int readTimeout = 5000;

	public HLStoYTB() throws LicensingException
	{
		super();
	}

	@Override
	public void init(IApplicationInstance appInstance, String streamName, IMediaStream stream, Map<String, String> profileData, Map<String, String> maps, IPushPublisher pushPublisher, boolean streamDebug)
	{

		String localEntryName = PushPublishUtils.getMapString(maps, "entryName");

		// playlistCrossName must be unique to the application Instance.
		this.playlistCrossName = "pushpublish-cupertino-http-playlists-" + appInstance.getContextStr() + "-" + streamName + "-" + localEntryName;

		// Call super.init() to initialize this profile and trigger call to our load() method
		super.init(appInstance, streamName, stream, profileData, maps, pushPublisher, streamDebug);
	}

	@Override
	public void load(HashMap<String, String> dataMap)
	{
		System.out.println("load: " + dataMap);
		super.load(dataMap);

		httpHost = hostname;
		String httpHostStr = PushPublishUtils.removeMapString(dataMap, "host");
		if (!StringUtils.isEmpty(httpHostStr))
			httpHost = httpHostStr;

		String sendSSLStr = PushPublishUtils.removeMapString(dataMap, "sendSSL");
		if (sendSSLStr != null)
		{
			sendSSLStr = sendSSLStr.toLowerCase(Locale.ENGLISH);
			isSendSSL = sendSSLStr.startsWith("t") || sendSSLStr.startsWith("y");
		}
		String LogDetailStr = PushPublishUtils.removeMapString(dataMap, "LogDetail");
		if (LogDetailStr != null)
		{
			LogDetailStr = LogDetailStr.toLowerCase(Locale.ENGLISH);
			LogDetail = sendSSLStr.startsWith("t") || sendSSLStr.startsWith("y");
		}
		logInfo("LogDetail : ", String.valueOf(LogDetail) );
		// set default http(s) port if it hasn't been changed from the default rtmp port.
		if (port == 1935)
		{
			port = isSendSSL ? DEFAULT_HTTPS_PORT : DEFAULT_HTTP_PORT;
		}
		logInfo("by EQMaker", "Load complete");
	}

	@Override
	public boolean updateGroupMasterPlaylistPlaybackURI(String groupName, PlaylistModel masterPlaylist)	{ return true;	}

	@Override
	public boolean updateMasterPlaylistPlaybackURI(PlaylistModel playlist) { return true;	}

	@Override
	public boolean updateMediaPlaylistPlaybackURI(PlaylistModel playlist)
	{
		boolean retVal = true;

		String path = playlist.getUri().toString();
		try
		{
			playlist.setUri(new URI(path));
		}
		catch (URISyntaxException e)
		{
			logError("updateMediaPlaylistPlaybackURI", "Failed to update media playlist to " + path);
			retVal = false;
		}
		return retVal;
	}

	@Override
	public boolean updateMediaSegmentPlaybackURI(MediaSegmentModel mediaSegment)
	{
		boolean retVal = true;
		String newPath = mediaSegment.getUri().getPath();
		try
		{
			String temp = newPath;
			mediaSegment.setUri(new URI(temp));
		}
		catch (Exception e)
		{
			retVal = false;
			logError("updateMediaSegmentPlaybackURI", "Invalid path " + newPath, e);
		}
		return retVal;
	}

	@Override
	public int sendGroupMasterPlaylist(String groupName, PlaylistModel playlist){return 1;}

	@Override
	public int sendMasterPlaylist(PlaylistModel playlist){return 1;	}

	@Override
	public int sendMediaPlaylist(PlaylistModel playlist)
	{
		int retVal = 0;
		String playlistPath = playlist.getUri().getPath();
		retVal = writePlaylist(playlist, playlistPath);
		return retVal;
	}

	@Override
	public int sendMediaSegment(MediaSegmentModel mediaSegment)
	{
		int size = 0;
		URL url = null;
		HttpURLConnection conn = null;
		try
		{
			PacketFragmentList list = mediaSegment.getFragmentList();
			if (list != null && list.size() != 0)
			{
				url = new URL((isSendSSL ? "https://" : "http://") + httpHost + mediaSegment.getUri());
				if (LogDetail == true) { logInfo("sendMediaSegmentTarget", url.toString() ); }
				conn = (HttpURLConnection)url.openConnection();
				conn.setConnectTimeout(connectionTimeout);
				conn.setReadTimeout(readTimeout);
				conn.setRequestMethod("PUT");
				conn.setDoOutput(true);

				Iterator<IPacketFragment> itr = list.getFragments().iterator();
				while (itr.hasNext())
				{
					IPacketFragment fragment = itr.next();
					if (fragment.getLen() <= 0)
						continue;
					byte[] data = fragment.getBuffer();

					conn.getOutputStream().write(data);
					size += data.length;
				}
				int status = conn.getResponseCode();
				if (LogDetail == true) { logInfo("sendMediaSegmentTarget", "Response : " + String.valueOf(status) ); }
				if (status < 200 || status >= 300)
					size = 0;
			}
			else
				size = 1;  // empty fragment list.
		}
		catch (Exception e)
		{
			logError("sendMediaSegment", "Failed to send media segment data to " + url.toString(), e);
			size = 0;
		}
		finally
		{
			if (conn != null)
			{
				conn.disconnect();
			}
		}
		return size;
	}

	@Override
	public int deleteMediaSegment(MediaSegmentModel mediaSegment){return 1;}

	@Override
	public void setSendToBackupServer(boolean backup){this.backup = backup;}

	@Override
	public boolean isSendToBackupServer(){return backup;}

	@Override
	public boolean outputOpen(){return true;}

	@Override
	public boolean outputClose(){return true;}

	@Override
	public String getDestionationLogData()
	{	return "HLStoYTBv1.29 Set target [" + (isSendSSL ? "https://" : "http://") + httpHost +"]";	}

	private int writePlaylist(PlaylistModel playlist, String playlistPath)
	{
		System.out.println("***********************************" + playlistPath);
		int retVal = 0;
		URL url = null;
		HttpURLConnection conn = null;
		try
		{
			url = new URL((isSendSSL ? "https://" : "http://") + httpHost + playlistPath);
			if (LogDetail == true) { logInfo("writePlaylistTarget", url.toString() ); }
			conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(connectionTimeout);
			conn.setReadTimeout(readTimeout);
			conn.setDoOutput(true);
			conn.setRequestMethod("PUT");

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PlaylistWriter writer = new PlaylistWriter(out, getContextStr());	
			String outStr = "";
			
			if (writer.write(playlist))
			{

				outStr = out.toString();
				if (LogDetail == true) { logInfo("writePlaylistTarget", "Playlist generation\n" + outStr);}
				byte[] bytes = outStr.getBytes();
				conn.getOutputStream().write(bytes); 
				retVal = bytes.length;
				
			}
			
			int status = conn.getResponseCode();
			if (LogDetail == true) { logInfo("writePlaylistTarget", "Response : " + String.valueOf(status)); }
			if (status < 200 || status >= 300)
			{
				retVal = 0;
				logWarn("writePlaylist", "Failed to send playlist data to " + playlist.getUri() + ", http status: " + status + ", playlist: " + outStr);
			}

		}
		catch (Exception e)
		{
			logError("writePlaylist", "Failed to send playlist data to " + url.toString(), e);
			retVal = 0;
		}
		finally
		{
			if (conn != null)
			{
				conn.disconnect();
			}
		}
		return retVal;
	}

	private String getDestinationPath()
	{
		if (!backup)
			return basePath + getDstStreamName();
		return basePath + getDstStreamName() + "-b";
	}

	private String getPortStr()
	{
		String portStr = "";
		if (port != DEFAULT_HTTP_PORT && port != DEFAULT_HTTPS_PORT)
			portStr = ":" + port;
		return portStr;
	}

}
