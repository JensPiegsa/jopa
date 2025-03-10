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
package cz.cvut.kbss.jopa.exceptions;

/**
 * This exception is thrown when an exception occurs while accessing the OntoDriver.
 */
public class StorageAccessException extends OWLPersistenceException {

    private static final long serialVersionUID = 4661531292404254252L;

    public StorageAccessException(String message) {
        super(message);
    }

    public StorageAccessException(Throwable cause) {
        super(cause);
    }

    public StorageAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
