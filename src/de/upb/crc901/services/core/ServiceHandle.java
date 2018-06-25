package de.upb.crc901.services.core;

import java.io.Serializable;
import java.util.Objects;

public final class ServiceHandle implements Serializable {
  /* default values */
  private static final String OWN_HOST = "local";

  private static final Object service_placeholder = new Object();

  private static final String not_serialized_id = "NULL";

  private static final String not_specified_classpath = "NO_CLASS_SPECIFIED";

  /**
   *
   */
  private final String host;
  private final String classpath;
  private final String id;
  private final transient Object service;


  /**
   * Standard constructor
   *
   * @param id
   *          Id which the service can be accessed through
   * @param service
   *          inner service
   */
  public ServiceHandle(final String host, final String classpath, final String id, final Object service) {
    super();
    this.host = Objects.requireNonNull(host);
    this.classpath = Objects.requireNonNull(classpath);
    this.id = Objects.requireNonNull(id);
    this.service = service;
  }

  /**
   * Contructor for local host.
   */
  public ServiceHandle(final String classpath, final String id, final Object service) {
    this(OWN_HOST, classpath, id, service);
  }

  /**
   * Constructor for external host.
   */
  public ServiceHandle(final String classpath, final String id) {
    this(classpath, id, service_placeholder);
  }

  /**
   * Empty constructor to create service handle with default values.
   */
  public ServiceHandle() {
    this(OWN_HOST, not_specified_classpath, not_serialized_id, service_placeholder);
  }

  /**
   * Returns the id of the service. Throws Runtime-Exception if wasSerialized() returns false.
   */
  public String getId() {
    // assert(isSerialized()); // needs to be serialized before accessing it's id.
    return this.id;
  }

  /**
   *
   * @return The actual service object.
   */
  public Object getService() {
    return this.service;
  }

  /**
   *
   * @return Host of this service.
   */
  public String getHost() {
    return this.host;
  }


  public String getClasspath() {
    return this.classpath;
  }

  /**
   * @return True if the service is only remotely accessible.
   */
  public boolean isRemote() {
    return !OWN_HOST.equals(this.getHost());
  }

  /**
   *
   * @return The address under which the service is accessible.
   */
  public String getServiceAddress() {
    assert (this.isRemote()); // No need to access address if you are the host.
    return this.getHost() + "/" + this.getClasspath() + "/" + this.getId();
  }

  public boolean isSerialized() {
    return !this.id.equals(not_serialized_id);
  }

  public boolean isClassPathSpecified() {
    return !this.classpath.equals(not_specified_classpath);
  }

  /**
   * Returns a copy of this object with the given host.
   */
  public ServiceHandle withExternalHost(final String otherHost) {
    Objects.requireNonNull(otherHost);
    if (otherHost.equals(this.getHost())) {
      // otherhost changes nothing
      return this;
    }
    return new ServiceHandle(otherHost, this.getClasspath(), this.getId(), this.getService());
  }

  public ServiceHandle withLocalHost() {
    return this.withExternalHost(OWN_HOST);
  }

  /**
   * Returns a copy of this object with the given host.
   */
  public ServiceHandle withClassPath(final String classPath) {
    Objects.requireNonNull(classPath);
    if (classPath.equals(this.getClasspath())) {
      // otherhost changes nothing
      return this;
    }
    return new ServiceHandle(this.getHost(), classPath, this.getId(), this.getService());
  }

  public ServiceHandle withService(final Object service) {
    Objects.requireNonNull(service);
    return new ServiceHandle(this.getHost(), this.getClasspath(), this.getId(), service);
  }

  public ServiceHandle withId(final String id2) {
    Objects.requireNonNull(id2);
    return new ServiceHandle(this.getHost(), this.getClasspath(), id2, this.getService());
  }

  /**
   * Returns a copy of this object with empty id.
   */
  public ServiceHandle unsuccessedSerialize() {
    return new ServiceHandle(this.getHost(), this.getClasspath(), not_serialized_id, this.getService());
  }

	public boolean containService() {
    return service_placeholder != this.getService();
  }
}