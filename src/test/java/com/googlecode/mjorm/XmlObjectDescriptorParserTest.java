package com.googlecode.mjorm;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class XmlObjectDescriptorParserTest {

	private XmlObjectDescriptorParser parser = null;

	@Before
	public void setUp()
		throws Exception {
		parser = new XmlObjectDescriptorParser();
	}

	@After
	public void tearDown()
		throws Exception {
		parser = null;
	}

	private InputStream res(String path) {
		return getClass().getResourceAsStream(path);
	}

	@Test
	public void testParseDocument_City()
		throws Exception {
		
		List<ObjectDescriptor> descriptors
			= parser.parseDocument(res("/com/googlecode/mjorm/City.mongo.xml"));
		assertNotNull(descriptors);
		assertEquals(1, descriptors.size());

		ObjectDescriptor cityDescriptor = descriptors.get(0);
		assertEquals(3, cityDescriptor.getProperties().size());

		PropertyDescriptor nameDesc = cityDescriptor.getPropertyDescriptor("name");
		PropertyDescriptor latDesc = cityDescriptor.getPropertyDescriptor("lat");
		PropertyDescriptor lonDesc = cityDescriptor.getPropertyDescriptor("lon");
		assertNotNull(nameDesc);
		assertNotNull(latDesc);
		assertNotNull(lonDesc);

		assertEquals(String.class, nameDesc.getType());
		assertEquals(BigDecimal.class, latDesc.getType());
		assertEquals(BigDecimal.class, lonDesc.getType());
	}

	@Test
	public void testParseDocument_SuperDuperOverride()
		throws Exception {
		
		List<ObjectDescriptor> descriptors
			= parser.parseDocument(res("/com/googlecode/mjorm/SuperDuperOverride.mongo.xml"));
		assertNotNull(descriptors);
		assertEquals(1, descriptors.size());

		ObjectDescriptor sdDescriptor = descriptors.get(0);
		assertEquals(5, sdDescriptor.getProperties().size());

		PropertyDescriptor personSet = sdDescriptor.getPropertyDescriptor("personSet");
		PropertyDescriptor personList = sdDescriptor.getPropertyDescriptor("personList");
		PropertyDescriptor personSortedSet = sdDescriptor.getPropertyDescriptor("personSortedSet");
		PropertyDescriptor personMap = sdDescriptor.getPropertyDescriptor("personMap");
		PropertyDescriptor stringMap = sdDescriptor.getPropertyDescriptor("stringMap");
		assertNotNull(personSet);
		assertNotNull(personSet.getParameterTypes());
		assertEquals(PersonEx.class, personSet.getParameterTypes()[0]);
		assertNotNull(personList);
		assertNotNull(personSortedSet);
		assertNotNull(personMap);
		assertNotNull(stringMap);
		
		assertEquals(Set.class, personSet.getType());
		assertEquals(PersonEx.class, personSet.getParameterTypes()[0]);
		assertEquals(List.class, personList.getType());
		assertEquals(SortedSet.class, personSortedSet.getType());
		assertEquals(Map.class, personMap.getType());
		assertEquals(Map.class, stringMap.getType());
	}

	@Test
	public void testParseDocument_City_And_SuperDuperOverride()
		throws Exception {
		
		List<ObjectDescriptor> descriptors
			= parser.parseDocument(res("/com/googlecode/mjorm/City_And_SuperDuperOverride.mongo.xml"));
		assertNotNull(descriptors);
		assertEquals(2, descriptors.size());

		ObjectDescriptor cityDescriptor = descriptors.get(0);
		assertEquals(3, cityDescriptor.getProperties().size());

		PropertyDescriptor nameDesc = cityDescriptor.getPropertyDescriptor("name");
		PropertyDescriptor latDesc = cityDescriptor.getPropertyDescriptor("lat");
		PropertyDescriptor lonDesc = cityDescriptor.getPropertyDescriptor("lon");
		assertNotNull(nameDesc);
		assertNotNull(latDesc);
		assertNotNull(lonDesc);

		assertEquals(String.class, nameDesc.getType());
		assertEquals(BigDecimal.class, latDesc.getType());
		assertEquals(BigDecimal.class, lonDesc.getType());

		ObjectDescriptor sdDescriptor = descriptors.get(1);
		assertEquals(5, sdDescriptor.getProperties().size());

		PropertyDescriptor personSet = sdDescriptor.getPropertyDescriptor("personSet");
		PropertyDescriptor personList = sdDescriptor.getPropertyDescriptor("personList");
		PropertyDescriptor personSortedSet = sdDescriptor.getPropertyDescriptor("personSortedSet");
		PropertyDescriptor personMap = sdDescriptor.getPropertyDescriptor("personMap");
		PropertyDescriptor stringMap = sdDescriptor.getPropertyDescriptor("stringMap");
		assertNotNull(personSet);
		assertNotNull(personSet.getParameterTypes());
		assertEquals(PersonEx.class, personSet.getParameterTypes()[0]);
		assertNotNull(personList);
		assertNotNull(personSortedSet);
		assertNotNull(personMap);
		assertNotNull(stringMap);
		
		assertEquals(Set.class, personSet.getType());
		assertEquals(PersonEx.class, personSet.getParameterTypes()[0]);
		assertEquals(List.class, personList.getType());
		assertEquals(SortedSet.class, personSortedSet.getType());
		assertEquals(Map.class, personMap.getType());
		assertEquals(Map.class, stringMap.getType());
	}

}
