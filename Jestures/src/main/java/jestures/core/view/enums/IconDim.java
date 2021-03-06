/*******************************************************************************
 * Copyright (c) 2018 Giulianini Luca
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package jestures.core.view.enums;

/**
 * Enumeration for text. You can set all the dimensions.
 *
 */
public enum IconDim {
    /**
     * Dims.
     */
    BIGGEST(80), BIGGER(60), BIG(40), MEDIUM(30), SMALL(20), SMALLER(15), SMALLEST(10);

    private int dim;

    IconDim(final int size) {
        this.dim = size;
    }

    /**
     * Get the dim.
     *
     * @return the dim
     */
    public int getDim() {
        return this.dim;
    }
}