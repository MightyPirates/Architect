/**
 * Welcome to the Architect API, step right in, we have cookies.
 * <p>
 * I assume you're here to register custom block converters to allow them being
 * stored in and placed via blueprints.
 * <p>
 * To do so, have a look at implement a converter using the {@link li.cil.architect.api.converter.Converter}
 * interface, then register the provider with Architect via {@link li.cil.architect.api.ConverterAPI#addConverter(Converter)}.
 * <p>
 * The converter will then be queried by Architect when needed.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@net.minecraftforge.fml.common.API(
        owner = API.MOD_ID,
        provides = API.MOD_ID + "API",
        apiVersion = API.MOD_VERSION)
package li.cil.architect.api;

import li.cil.architect.api.converter.Converter;
import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;