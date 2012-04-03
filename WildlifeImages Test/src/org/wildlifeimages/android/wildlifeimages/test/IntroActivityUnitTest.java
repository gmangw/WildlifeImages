package org.wildlifeimages.android.wildlifeimages.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.wildlifeimages.android.wildlifeimages.ExhibitListActivity;
import org.wildlifeimages.android.wildlifeimages.IntroActivity;
import org.wildlifeimages.android.wildlifeimages.MapActivity;
import org.wildlifeimages.android.wildlifeimages.PhotosActivity;

import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;
import android.util.Log;
import android.view.View;
import org.wildlifeimages.android.wildlifeimages.R;
import org.xml.sax.SAXException;

public class IntroActivityUnitTest extends ActivityUnitTestCase<IntroActivity>{

	Intent mockIntent = new Intent(Intent.ACTION_MAIN);

	public IntroActivityUnitTest() {
		super(IntroActivity.class);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void test(){
		IntroActivity mActivity = startActivity(mockIntent, null, null);
		assertNotNull(mActivity);

		View v = new View(mActivity);

		v.setId(-1);
		mActivity.introProcessSidebar(v);
		assertNull(getStartedActivityIntent());

		v.setId(R.id.intro_sidebar_events);
		mActivity.introProcessSidebar(v);
		assertNotNull(getStartedActivityIntent());
		assertEquals(PhotosActivity.class.getName(), getStartedActivityIntent().getComponent().getClassName());

		v.setId(R.id.intro_sidebar_photos);
		mActivity.introProcessSidebar(v);
		assertNotNull(getStartedActivityIntent());
		assertEquals(PhotosActivity.class.getName(), getStartedActivityIntent().getComponent().getClassName());

		v.setId(R.id.intro_sidebar_map);
		mActivity.introProcessSidebar(v);
		assertNotNull(getStartedActivityIntent());
		assertEquals(MapActivity.class.getName(), getStartedActivityIntent().getComponent().getClassName());

		v.setId(R.id.intro_sidebar_exhibitlist);
		mActivity.introProcessSidebar(v);
		assertNotNull(getStartedActivityIntent());
		assertEquals(ExhibitListActivity.class.getName(), getStartedActivityIntent().getComponent().getClassName());

		getInstrumentation().callActivityOnPause(mActivity);
		getInstrumentation().callActivityOnResume(mActivity);
		getInstrumentation().callActivityOnSaveInstanceState(mActivity, new Bundle());
	}

	public void testXML() throws IOException, SAXException, ParserConfigurationException{
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO){
			IntroActivity mActivity = startActivity(mockIntent, null, null);
			assertNotNull(mActivity);

			File schema = new File(mActivity.getCacheDir(), "ExhibitsSchema.xsd");
			schema.createNewFile();

			FileOutputStream outStream = new FileOutputStream(schema);

			InputStream inStream = getInstrumentation().getContext().getAssets().open("ExhibitsSchema.xsd");

			for (int value = inStream.read(); value != -1; value = inStream.read()){
				outStream.write(value);
			}
			inStream.close();
			outStream.close();

			File container = new File(schema.getParentFile(), "exhibits");
			container.mkdir();
			File exhibits = new File(container, "exhibits.xml");

			outStream = new FileOutputStream(exhibits);

			inStream = mActivity.getAssets().open("exhibits.xml");

			for (int value = inStream.read(); value != -1; value = inStream.read()){
				if (((char)value) == '>' && false){
					outStream.write('<');
					Log.e(this.getClass().getName(), "replaced");
				}else{
					outStream.write(value);
				}
			}
			inStream.close();
			outStream.close();

			// parse an XML document into a DOM tree
			DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			DocumentBuilder parser = parserFactory.newDocumentBuilder();
			Document document = parser.parse(exhibits);

			// create a SchemaFactory capable of understanding WXS schemas
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

			// load a WXS schema, represented by a Schema instance
			Source schemaFile = new StreamSource(schema);
			Schema schemaData = factory.newSchema(schemaFile);

			// create a Validator instance, which can be used to validate an instance document
			Validator validator = schemaData.newValidator();

			// validate the DOM tree

			validator.validate(new DOMSource(document));


		}
	}
}
