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

import cz.cvut.kbss.jopa.test.environment.Rdf4jDataAccessor;
import cz.cvut.kbss.jopa.test.environment.Rdf4jPersistenceFactory;
import cz.cvut.kbss.jopa.test.runner.UpdateOperationsOnGetReferenceRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateOperationsOnGetReferenceTest extends UpdateOperationsOnGetReferenceRunner {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateOperationsOnGetReferenceTest.class);

    public UpdateOperationsOnGetReferenceTest() {
        super(LOG, new Rdf4jPersistenceFactory(), new Rdf4jDataAccessor());
    }
}
