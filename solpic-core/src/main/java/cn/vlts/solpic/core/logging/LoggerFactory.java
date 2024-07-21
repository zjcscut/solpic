package cn.vlts.solpic.core.logging;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Logger factory.
 *
 * @author throwable
 * @since 2024/7/20 23:49
 */
public final class LoggerFactory {

    private static final Map<String, String> CANDIDATE_CLASS_NAMES = new LinkedHashMap<>();

    private static volatile LoggerAdapter adapter;

    public static void setAdapter(LoggerAdapter adapter) {
        if (Objects.nonNull(adapter)) {
            if (LoggerFactory.adapter != adapter) {
                Logger logger = adapter.getLogger(LoggerFactory.class);
                LoggerFactory.adapter = adapter;
                logger.debug("Initializing logger adapter, type: " + adapter.getName());
            }
        }
    }

    public static void setLevel(LogLevel level) {
        Optional.ofNullable(adapter).ifPresent(v -> v.setLogLevel(level));
    }

    public static LogLevel getLevel() {
        return Optional.ofNullable(adapter).map(LoggerAdapter::getLogLevel).orElse(null);
    }

    public static Logger getLogger(Class<?> clazz) {
        return Optional.ofNullable(adapter).map(v -> v.getLogger(clazz))
                .orElseThrow(() -> new IllegalStateException("Create logger failed for logging framework internal error"));
    }

    public static Logger getLogger(String key) {
        return Optional.ofNullable(adapter).map(v -> v.getLogger(key))
                .orElseThrow(() -> new IllegalStateException("Create logger failed for logging framework internal error"));
    }

    public static List<String> getAvailableAdapterTypes() {
        return Collections.unmodifiableList(getAvailableAdapters(false).stream().map(LoggerAdapter::getName)
                .collect(Collectors.toList()));
    }

    public static List<LoggerAdapter> getAvailableAdapters() {
        return getAvailableAdapters(false);
    }

    public static List<LoggerAdapter> getAvailableAdapters(boolean shortcut) {
        List<LoggerAdapter> candidates = new ArrayList<>();
        for (Map.Entry<String, String> entry : CANDIDATE_CLASS_NAMES.entrySet()) {
            String candidateType = entry.getValue();
            try {
                LoggerAdapter loggerAdapter = (LoggerAdapter) Class.forName(candidateType).getDeclaredConstructor()
                        .newInstance();
                loggerAdapter.getLogger(LoggerFactory.class);
                candidates.add(loggerAdapter);
                if (shortcut) {
                    break;
                }
            } catch (Exception ignore) {

            }
        }
        return Collections.unmodifiableList(candidates);
    }

    static {
        // set default candidates
        CANDIDATE_CLASS_NAMES.put("slf4j", "cn.vlts.solpic.core.logging.Slf4jLoggerAdapter");
        CANDIDATE_CLASS_NAMES.put("log4j2", "cn.vlts.solpic.core.logging.Log4j2LoggerAdapter");
        CANDIDATE_CLASS_NAMES.put("logback", "cn.vlts.solpic.core.logging.LogbackLoggerAdapter");
        CANDIDATE_CLASS_NAMES.put("log4j", "cn.vlts.solpic.core.logging.Log4jLoggerAdapter");
        CANDIDATE_CLASS_NAMES.put("jcl", "cn.vlts.solpic.core.logging.JclLoggerAdapter");
        CANDIDATE_CLASS_NAMES.put("jul", "cn.vlts.solpic.core.logging.JulLoggerAdapter");
        // switch logging type
        String type = System.getProperty("solpic.logging.type", "");
        switch (type) {
            case Slf4jLoggerAdapter.NAME:
                setAdapter(new Slf4jLoggerAdapter());
                break;
            case Log4j2LoggerAdapter.NAME:
                setAdapter(new Log4j2LoggerAdapter());
                break;
            case LogbackLoggerAdapter.NAME:
                setAdapter(new LogbackLoggerAdapter());
                break;
            case Log4jLoggerAdapter.NAME:
                setAdapter(new Log4jLoggerAdapter());
                break;
            case JclLoggerAdapter.NAME:
                setAdapter(new JclLoggerAdapter());
                break;
            case JulLoggerAdapter.NAME:
                setAdapter(new JulLoggerAdapter());
                break;
            default:
                init();
        }
    }

    private static void init() {
        AtomicBoolean found = new AtomicBoolean();
        List<LoggerAdapter> availableAdapters = getAvailableAdapters(true);
        availableAdapters.stream().findFirst().ifPresent(availableAdapter -> {
            setAdapter(availableAdapter);
            found.set(true);
        });
        if (!found.get()) {
            System.out.println("Solpic: any logging framework adapter could not be found.");
        }
    }
}
