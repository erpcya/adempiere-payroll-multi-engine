/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 or later of the                                  *
 * GNU General Public License as published by the Free Software Foundation.   *
 *****************************************************************************/
package org.spin.eca59.concept;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.adempiere.exceptions.AdempiereException;
import org.eevolution.hr.model.MHRConcept;
import org.eevolution.hr.model.MHRPayrollConcept;
import org.junit.jupiter.api.Test;

import sun.misc.Unsafe;

class PayrollConceptTest {

	@Test
	void rejectsNullConcept() {
		AdempiereException exception = assertThrows(AdempiereException.class,
				() -> PayrollConcept.newInstance((MHRConcept) null));

		assertEquals("@HR_Concept_ID@ @IsMandatory@", exception.getMessage());
	}

	@Test
	void rejectsNullPayrollConcept() {
		AdempiereException exception = assertThrows(AdempiereException.class,
				() -> PayrollConcept.newInstance((MHRPayrollConcept) null));

		assertEquals("@HR_Concept_ID@ @IsMandatory@", exception.getMessage());
	}

	@Test
	void mapsConceptFieldsAndEnabledFlags() {
		MHRConcept source = TestConcept.newInstance(true);

		PayrollConcept concept = PayrollConcept.newInstance(source);

		assertEquals(0, concept.getId());
		assertEquals("SALARY", concept.getValue());
		assertEquals("Salary", concept.getName());
		assertEquals("Base salary", concept.getDescription());
		assertEquals("Monthly base salary", concept.getHelp());
		assertEquals("E", concept.getType());
		assertEquals("A", concept.getColumnType());
		assertEquals("D", concept.getAccountSign());
		assertTrue(concept.isManual());
		assertTrue(concept.isEmployee());
		assertTrue(concept.isInvoiced());
		assertTrue(concept.isPrinted());
		assertTrue(concept.isPaid());
		assertTrue(concept.isSaveInHistoric());
		assertTrue(concept.isNotSaveInHistoryIfNull());
		assertEquals(11, concept.getCategoryId());
		assertEquals(12, concept.getTypeId());
		assertEquals(20, concept.getSequence());
		assertEquals(2, concept.getPrecision());
		assertEquals(0, concept.getPayrollconceptId());
		assertSame(source, concept.getConcept());
		assertEquals("PayrollConcept [id=0, value=SALARY, name=Salary]", concept.toString());
	}

	@Test
	void mapsDisabledFlags() {
		MHRConcept source = TestConcept.newInstance(false);

		PayrollConcept concept = PayrollConcept.newInstance(source);

		assertFalse(concept.isManual());
		assertFalse(concept.isEmployee());
		assertFalse(concept.isInvoiced());
		assertFalse(concept.isPrinted());
		assertFalse(concept.isPaid());
		assertFalse(concept.isSaveInHistoric());
		assertFalse(concept.isNotSaveInHistoryIfNull());
	}

	private static class TestConcept extends MHRConcept {
		private boolean enabled;

		private TestConcept() {
			super(null, 0, null);
		}

		private static TestConcept newInstance(boolean enabled) {
			try {
				Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
				unsafeField.setAccessible(true);
				TestConcept concept = (TestConcept) ((Unsafe) unsafeField.get(null)).allocateInstance(TestConcept.class);
				concept.enabled = enabled;
				return concept;
			} catch (ReflectiveOperationException exception) {
				throw new AssertionError("Unable to create the test concept", exception);
			}
		}

		@Override
		public int getHR_Concept_ID() {
			return 0;
		}

		@Override
		public String getValue() {
			return "SALARY";
		}

		@Override
		public String getName() {
			return "Salary";
		}

		@Override
		public String getDescription() {
			return "Base salary";
		}

		@Override
		public String getHelp() {
			return "Monthly base salary";
		}

		@Override
		public String getType() {
			return "E";
		}

		@Override
		public String getColumnType() {
			return "A";
		}

		@Override
		public String getAccountSign() {
			return "D";
		}

		@Override
		public boolean isManual() {
			return enabled;
		}

		@Override
		public boolean isEmployee() {
			return enabled;
		}

		@Override
		public boolean isInvoiced() {
			return enabled;
		}

		@Override
		public boolean isPrinted() {
			return enabled;
		}

		@Override
		public boolean isPaid() {
			return enabled;
		}

		@Override
		public boolean isSaveInHistoric() {
			return enabled;
		}

		@Override
		public boolean isNotSaveInHistoryIfNull() {
			return enabled;
		}

		@Override
		public int getHR_Concept_Category_ID() {
			return 11;
		}

		@Override
		public int getHR_Concept_Type_ID() {
			return 12;
		}

		@Override
		public int getSeqNo() {
			return 20;
		}

		@Override
		public int getStdPrecision() {
			return 2;
		}
	}
}
