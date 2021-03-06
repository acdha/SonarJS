/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.javascript.api.visitors;

import java.io.IOException;
import java.net.URI;

public interface JavaScriptFile {

  /**
   * @deprecated since 4.0, use {@link JavaScriptFile#fileName()} or {@link JavaScriptFile#uri()}
   */
  @Deprecated
  String relativePath();

  /**
   * File name with extension
   */
  String fileName();

  String contents() throws IOException;

  /**
   * Identifier of the file. The only guarantee is that it is unique in the project.
   * You should not assume it is a file:// URI.
   */
  URI uri();
}
