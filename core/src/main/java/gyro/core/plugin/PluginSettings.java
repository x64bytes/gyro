package gyro.core.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import gyro.core.resource.RootScope;
import gyro.core.resource.Settings;

public class PluginSettings extends Settings {

    private List<Plugin> plugins;
    private List<Class<?>> otherClasses;

    private final LoadingCache<Plugin, LoadingCache<Class<?>, Boolean>> call = CacheBuilder.newBuilder()
        .build(new CacheLoader<Plugin, LoadingCache<Class<?>, Boolean>>() {

            @Override
            public LoadingCache<Class<?>, Boolean> load(Plugin plugin) {
                return CacheBuilder.newBuilder()
                    .build(new CacheLoader<Class<?>, Boolean>() {

                        @Override
                        public Boolean load(Class<?> otherClass) throws Exception {
                            plugin.onEachClass((RootScope) getScope(), otherClass);
                            return Boolean.TRUE;
                        }
                    });
            }
        });

    public List<Plugin> getPlugins() {
        if (plugins == null) {
            plugins = new ArrayList<>();
        }

        return plugins;
    }

    public void setPlugins(List<Plugin> plugins) {
        this.plugins = plugins;
    }

    public List<Class<?>> getOtherClasses() {
        if (otherClasses == null) {
            otherClasses = new ArrayList<>();
        }

        return otherClasses;
    }

    public void setOtherClasses(List<Class<?>> otherClasses) {
        this.otherClasses = otherClasses;
    }

    public void addClasses(Set<Class<?>> classes) throws Exception {
        List<Plugin> plugins = getPlugins();
        List<Class<?>> otherClasses = getOtherClasses();

        for (Class<?> c : classes) {
            if (Plugin.class.isAssignableFrom(c)) {
                plugins.add((Plugin) c.newInstance());

            } else {
                otherClasses.add(c);
            }
        }

        for (Plugin plugin : plugins) {
            for (Class<?> otherClass : otherClasses) {
                try {
                    call.get(plugin).get(otherClass);

                } catch (ExecutionException error) {
                    Throwable cause = error.getCause();

                    if (cause instanceof Error) {
                        throw (Error) cause;

                    } else if (cause instanceof Exception) {
                        throw (Exception) cause;
                    }
                }
            }
        }
    }

}
