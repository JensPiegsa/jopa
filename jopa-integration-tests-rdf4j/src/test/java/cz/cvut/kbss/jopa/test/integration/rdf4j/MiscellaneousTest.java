/**
 * Copyright (C) 2023 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jopa.test.integration.rdf4j;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.test.environment.Rdf4jPersistenceFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiscellaneousTest {

    private final Rdf4jPersistenceFactory persistenceFactory;

    MiscellaneousTest() {
        this.persistenceFactory = new Rdf4jPersistenceFactory();
    }

    @Test
    void unwrapExtractsRdf4jRepository() {
        final EntityManager em = persistenceFactory.getEntityManager("UnwrapTest", false, Collections.emptyMap());
        try {
            final Repository repository = em.unwrap(Repository.class);
            assertNotNull(repository);
            assertTrue(repository.isInitialized());
        } finally {
            em.close();
        }
    }
}
