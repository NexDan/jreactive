# JReactive Documentation

Complete documentation for the JReactive library.

## 📚 Core Documentation

### [Single, Maybe & Completable Guide](SINGLE_MAYBE_COMPLETABLE.md)
Comprehensive guide to the specialized reactive types:
- **Single<T>**: Exactly one element or error
- **Maybe<T>**: Zero or one element
- **Completable**: Only completion or error

Learn when to use each type and see practical examples.

---

## 🔥 Advanced Topics

### [Subjects Implementation](SUBJECTS.md)
Hot Observables and multicasting with Subjects:
- **PublishSubject**: Multicast to current subscribers
- **BehaviorSubject**: Emits last value to new subscribers
- **ReplaySubject**: Replays N previous values
- **AsyncSubject**: Emits only the last value on completion

### [Specialized Types Summary](SPECIALIZED_TYPES.md)
Overview of all specialized reactive types including:
- Conversion methods between types
- Performance characteristics
- Best practices and use cases

### [Connectable Observables - Known Limitations](CONNECTABLE_LIMITATIONS.md)
Important considerations when working with ConnectableObservable:
- Threading limitations
- Known issues
- Workarounds and solutions

---

## ⚡ Performance & Benchmarks

### [Benchmarks Overview](benchmarks/BENCHMARKS.md)
Complete benchmarking setup and methodology:
- JMH configuration
- Benchmark categories
- How to run benchmarks

### [Benchmark Results](benchmarks/RESULTS.md)
Comparative performance results vs RxJava:
- Creation operators
- Transformation operators
- Filtering and combination operators
- Performance analysis and conclusions

### [Specialized Types Benchmark Results](benchmarks/SPECIALIZED_TYPES_RESULTS.md)
Performance comparison for Single, Maybe, and Completable:
- Creation benchmarks
- Transformation benchmarks
- Error handling performance

---

## 🚀 Quick Links

- [Main README](../README.md) - Project overview and getting started
- [Contributing Guide](../CONTRIBUTING.md) - How to contribute to JReactive
- [Issue Templates](../.github/ISSUE_TEMPLATE/) - Bug reports and feature requests

---

## 📖 Documentation Index

```
docs/
├── README.md (this file)
├── SINGLE_MAYBE_COMPLETABLE.md    # Specialized types guide
├── SUBJECTS.md                     # Subjects implementation
├── SPECIALIZED_TYPES.md            # Specialized types summary
├── CONNECTABLE_LIMITATIONS.md      # Known limitations
└── benchmarks/
    ├── BENCHMARKS.md               # Benchmark methodology
    ├── RESULTS.md                  # Benchmark results
    └── SPECIALIZED_TYPES_RESULTS.md # Specialized types results
```

---

**Need help?** Check the [main README](../README.md) or open an [issue](https://github.com/yasmramos/jreactive/issues).
