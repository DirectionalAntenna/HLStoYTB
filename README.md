<h1>HLStoYTB (HLS publish to YouTube)</h1>
<p>HLStoYTB (HLS push publish to YouTube) module enables Wowza Streaming Engine to <strong>stream to YouTube Live channels using HLS push</strong>.</p>

## Table of Contents
- [Prerequisites](#Prerequisites)
- [How To Use](#HowToUse)
- [Install the module](#HowToInstall)
- [Environment Setup](#Environment)
- [Target Configuration](#ConfigureTarget)
- [Restart](#RestartEngine)
- [Start publish](#Running)
- [Contact](#Contact)
- [License](#License)

<h2 id="Prerequisites">Prerequisites</h2>
<ul><li>Wowza Streaming Engine 4.5.0 or later.</li>
<li>HLS ingest URL of YouTube live streaming channel.</li></ul>

<h2 id="HowToUse">How To Use</h2>
<h3 id="HowToInstall">1. Install the module to Wowza Streaming Engine</h3>
<p>To install the module, follow these steps</p>
<ol><li>Download <code>HLStoYTB.jar</code> or compile your own jar file from source.</li>
<li>Copy the jar file to the <code>lib</code> folder of Wowza Streaming Engine. <code>[Wowza Streaming Engine install-dir]/lib</code>.</li>
<li>Restart Wowza Streaming Engine.</li></ol>

<h3 id="Environment">2. Environment Setup</h3>
<p>Confirm the Wowza Streaming Engine configuration for the module's operation.</p>
<ol><li><strong>Verify Apple HLS is enabled</strong><p>Confirm that Apple HLS is activated in the Playback Types of the Live Application you want to use with the module.</p></li>
<li><strong>Adjust HLS Chunk</strong>
<p>Set an appropriate value for the <code>cupertinoChunkDurationTarget</code> in the <strong>Cupertino Streaming Packetizer</strong> section of the Live Application's properties. YouTube recommends a value between <var>2000</var> and <var>4000</var>, and the module developer suggests using <var>4000</var>.</p></li>
<li><strong>Enable Stream Target</strong><p>Enable the Streaming Target feature for the Live Application, unless it is already enabled.</p></li>
<li><strong>Add server property</strong><p>In the Properties section of Server Setup, add the following custom property.<p>
	<ul><li><strong>Path</strong> : <kbd>/Root/Server</kbd></li>
	<li><strong>Name</strong> : <kbd>pushPublishProfilesCustomFile</kbd></li>
	<li><strong>Type</strong> : <kbd>String</kbd></li>
	<li><strong>Value</strong> : <kbd>${com.wowza.wms.ConfigHome}/conf/PushPublishProfilesCustom.xml</kbd></li></ul>
<p>You can also You can also manually add the following block to the <code>&lt;Properties&gt;</code> section of the <code>[Wowza Streaming Engine install-dir]/conf/Server.xml</code> file, referring to the content below.</p>
<pre><code>&lt;Property&gt;
    &lt;Name&gt;pushPublishProfilesCustomFile&lt;/Name&gt;
    &lt;Value&gt;${com.wowza.wms.ConfigHome}/conf/PushPublishProfilesCustom.xml&lt;/Value&gt;
&lt;/Property&gt;</code></pre></li>
<li><strong>Edit <code>PushPublishProfilesCustom.xml</code></strong><p>Refer to the content below and add the <code>&lt;PushPublishProfile&gt;</code> block to the <code>&lt;PushPublishProfiles&gt;</code> section of the <code>[Wowza Streaming Engine install-dir]/conf/PushPublishProfilesCustom.xml</code> file.</p>
<p>If the file does not exist, create a new one.</p>
<pre><code>&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;Root&gt;
    &lt;PushPublishProfiles&gt;
        <strong>&lt;PushPublishProfile&gt;
            &lt;Name&gt;Youtube-HLS&lt;/Name&gt;
            &lt;Protocol&gt;HTTP&lt;/Protocol&gt;
            &lt;BaseClass&gt;kr.eqmaker.wms.hls.push.youtube.HLStoYTB&lt;/BaseClass&gt;
            &lt;UtilClass&gt;&lt;/UtilClass&gt;
            &lt;HTTPConfiguration&gt;
            &lt;/HTTPConfiguration&gt;
            &lt;Properties&gt;
            &lt;/Properties&gt;
        &lt;/PushPublishProfile&gt;</strong>
    &lt;/PushPublishProfiles&gt;
&lt;/Root&gt;</code></pre></li>
</ol>

<h3 id="ConfigureTarget">3. Target Configuration</h3>
<p>Set the Target Destination in the Map of the Live Application.</p> 
<p>Assume the YouTube ingest URL is <code>https://a.upload.youtube.com/http_upload_hls?cid=eqma-kere-qmak-ereq-make&amp;copy=0&amp;file=</code></p>

<p>Open the <code>PushPublishMap.txt</code> file located at <code>[Wowza install-dir]/conf/[Live Application Name]/</code> and add the content below Refer to the explanation.</p>
<pre><code>InputStream={"entryName":"HLStoYTB1", "profile":"HLS-Youtube", "streamName":"HLStoYTB1", "host":"a.upload.youtube.com/http_upload_hls?cid=eqma-kere-qmak-ereq-make&amp;copy=0&amp;file=", "sendSSL":"true"}</code></pre>
<dl>
<dt><code>InputStream</code></dt><dd>The name of the incoming stream to publish to YouTube.</dd>
<dt><code>entryName</code></dt><dd>The name of the entry in the PushPublishMap.txt file. This name must be unique.</dd>
<dt><code>streamName</code></dt><dd>The name of output stream. This name must be unique.</dd>
<dt><code>host</code></dt><dd>A HLS ingest URL issued by YouTube.</dd>
</dl>
<p>The unexplained items <code>profile</code> and <code>sendSSL</code> must remain unchanged; do not modify them, as this will cause the module to malfunction.</p>
<p>You can add entries to transmit additional streams or to send a single stream to multiple targets.</p>

<h3 id="RestartEngine">4. Restart Wowza Streaming Engine</h3>
<p>Restart the Wowza Streaming Engine to apply all changes.</p>

<h3 id="Running">5. Start publish</h3>
<p>When an InputStream is detected, the stream target automatically performs the HLS publishing action. The item will not be displayed in the UI's Stream Target list. You can check the operation status through the Wowza Streaming Engine logs and YouTube.</p>

<h2 id="Contact">Contact</h2>
<a href="mailto:eqmaker@eqmaker.kr">Mail to EQMaker</a> or 
<a href="https://www.decteng.com/" name="A Decent Engineer" target="_blank" hreflang="ko-kr">Leave a message</a>

<h2 id="License">License</h2>
<p>This code is distributed under the BSD 3-Clause License.</p>
<p><strong>About kr.eqmaker.wms.hls.push.youtube</strong><br>
This code of the package(kr.eqmaker.wms.hls.push.youtube) Copyright EQMaker All rights reserved.<br>
This code is based on wse-example-pushpublish-hls of Wowza Media Systems, LLC.<br>
This code is licensed pursuant to the BSD 3-Clause License.<br>
EQMaker makes no warranties regarding the performance or operation of this program.<br>
The user assumes all responsibility and risk for the use of this code, package and program.</p>

<p><strong>About wse-example-pushpublish-hls</strong><br>
This code and all components (c) Copyright 2018, Wowza Media Systems, LLC. All rights reserved.<br>
This code is licensed pursuant to the BSD 3-Clause License.</p>
